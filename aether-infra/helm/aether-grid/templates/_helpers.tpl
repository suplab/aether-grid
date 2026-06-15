{{/*
Expand the name of the chart.
*/}}
{{- define "aether-grid.name" -}}
{{- default .Chart.Name .Values.global.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a fully-qualified release name, capped at 63 characters.
*/}}
{{- define "aether-grid.fullname" -}}
{{- $name := default .Chart.Name .Values.global.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Resolve the namespace: prefer the explicit override, fall back to the Helm
release namespace so that `helm install --namespace foo` still works.
*/}}
{{- define "aether-grid.namespace" -}}
{{- .Values.global.namespaceOverride | default .Release.Namespace }}
{{- end }}

{{/*
Common labels applied to every resource.
*/}}
{{- define "aether-grid.labels" -}}
app.kubernetes.io/name: {{ include "aether-grid.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: aether-grid
{{- end }}

{{/*
Selector labels — stable subset used in matchLabels and selector.
Must NOT change after initial install (would break rolling update).
*/}}
{{- define "aether-grid.selectorLabels" -}}
app.kubernetes.io/name: {{ include "aether-grid.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Fully-qualified image reference for aether-api.
Renders:  <global.imageRegistry>/<repository>:<tag>
The repository value already contains the full path after the registry prefix,
so we strip any existing registry prefix from it before joining.
*/}}
{{- define "aether-grid.api.image" -}}
{{- $registry := .Values.global.imageRegistry | trimSuffix "/" }}
{{- $repo := .Values.api.image.repository }}
{{- $tag  := .Values.api.image.tag | toString }}
{{- printf "%s/%s:%s" $registry (base $repo) $tag }}
{{- end }}

{{/*
Fully-qualified image reference for aether-proxy.
*/}}
{{- define "aether-grid.proxy.image" -}}
{{- $registry := .Values.global.imageRegistry | trimSuffix "/" }}
{{- $repo := .Values.proxy.image.repository }}
{{- $tag  := .Values.proxy.image.tag | toString }}
{{- printf "%s/%s:%s" $registry (base $repo) $tag }}
{{- end }}

{{/*
ServiceAccount name for aether-api.
When create=true  → use the canonical name: <fullname>-api
When create=false → fall back to "default" so the pod still gets a SA.
*/}}
{{- define "aether-grid.api.serviceAccountName" -}}
{{- if .Values.api.serviceAccount.create }}
{{- printf "%s-api" (include "aether-grid.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- "default" }}
{{- end }}
{{- end }}

{{/*
ServiceAccount name for aether-proxy.
*/}}
{{- define "aether-grid.proxy.serviceAccountName" -}}
{{- if .Values.proxy.serviceAccount.create }}
{{- printf "%s-proxy" (include "aether-grid.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- "default" }}
{{- end }}
{{- end }}
