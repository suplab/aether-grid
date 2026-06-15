# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /workspace

# Copy parent POM and all module POMs first so Maven can resolve the reactor
# and the dependency layer is cached independently of source changes.
COPY pom.xml .
COPY aether-core/pom.xml    aether-core/
COPY aether-memory/pom.xml  aether-memory/
COPY aether-agents/pom.xml  aether-agents/
COPY aether-policy/pom.xml  aether-policy/
COPY aether-proxy/pom.xml   aether-proxy/
COPY aether-api/pom.xml     aether-api/
COPY aether-infra/pom.xml   aether-infra/

# Resolve all dependencies into the local Maven cache before copying source.
# -B  = batch mode (no ANSI / progress output)
# -q  = quiet (errors only)
RUN mvn dependency:go-offline -B -q

# Copy source trees for every module that aether-proxy depends on (-am walks
# the reactor graph: aether-core, aether-policy) plus aether-proxy itself.
COPY aether-core/src    aether-core/src
COPY aether-policy/src  aether-policy/src
COPY aether-proxy/src   aether-proxy/src
COPY aether-infra/src   aether-infra/src

# Build aether-proxy and all of its intra-reactor module dependencies.
# --enable-preview is already wired through maven-compiler-plugin in parent POM;
# maven-surefire passes it via <argLine> — no extra flag needed here for build.
RUN mvn package -pl aether-proxy -am -DskipTests -B -q

# ── Stage 2: runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-noble AS runtime

# Create a dedicated non-root group and user (uid/gid 1000 matches the
# conventional K8s runAsUser: 1000 / fsGroup: 1000 pod security context).
RUN groupadd --gid 1000 aether \
 && useradd  --uid 1000 --gid aether --shell /bin/false --no-create-home aether

WORKDIR /app

# Copy only the fat jar produced by the spring-boot:repackage goal.
# --chown ensures the jar is owned by the non-root user from the start.
COPY --from=build --chown=aether:aether /workspace/aether-proxy/target/*.jar app.jar

USER aether

# Liveness probe via Spring Boot Actuator (requires management.endpoint.health
# groups with a liveness group wired in application.yml / K8s ConfigMap).
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

EXPOSE 8080

# JVM flags:
#   --enable-preview          — required: code compiled with --enable-preview
#                               must also run with --enable-preview
#   -XX:+UseContainerSupport  — respect cgroup memory/cpu limits (Java 21 default on,
#                               explicit for clarity)
#   -XX:MaxRAMPercentage=75.0 — use 75 % of the container memory limit
#   -XX:+ExitOnOutOfMemoryError — crash fast so K8s can restart cleanly
#   -Djava.security.egd       — faster SecureRandom seeding in containers
#
# No secrets or application config here — all runtime configuration is
# injected by K8s Secrets / ConfigMaps as environment variables.
ENTRYPOINT ["java", \
  "--enable-preview", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
