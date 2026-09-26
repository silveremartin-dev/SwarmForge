{{/*
Expand the name of the chart.
*/}}
{{- define "swarmforge.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "swarmforge.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Server component name
*/}}
{{- define "swarmforge.server.fullname" -}}
{{- printf "%s-server" (include "swarmforge.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Compute component name
*/}}
{{- define "swarmforge.compute.fullname" -}}
{{- printf "%s-compute" (include "swarmforge.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Web component name
*/}}
{{- define "swarmforge.web.fullname" -}}
{{- printf "%s-web" (include "swarmforge.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "swarmforge.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "swarmforge.labels" -}}
helm.sh/chart: {{ include "swarmforge.chart" . }}
{{ include "swarmforge.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels (global)
*/}}
{{- define "swarmforge.selectorLabels" -}}
app.kubernetes.io/name: {{ include "swarmforge.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Server selector labels
*/}}
{{- define "swarmforge.server.selectorLabels" -}}
{{ include "swarmforge.selectorLabels" . }}
app.kubernetes.io/component: server
{{- end }}

{{/*
Compute selector labels
*/}}
{{- define "swarmforge.compute.selectorLabels" -}}
{{ include "swarmforge.selectorLabels" . }}
app.kubernetes.io/component: compute
{{- end }}

{{/*
Web selector labels
*/}}
{{- define "swarmforge.web.selectorLabels" -}}
{{ include "swarmforge.selectorLabels" . }}
app.kubernetes.io/component: web
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "swarmforge.serviceAccountName" -}}
{{- if .Values.server.serviceAccount.create }}
{{- default (include "swarmforge.server.fullname" .) .Values.server.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.server.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
PostgreSQL Host
*/}}
{{- define "swarmforge.databaseHost" -}}
{{- if .Values.postgresql.enabled }}
{{- printf "%s-postgresql" .Release.Name }}
{{- else }}
{{- .Values.externalDatabase.host }}
{{- end }}
{{- end }}

{{/*
Redis Host
*/}}
{{- define "swarmforge.redisHost" -}}
{{- if .Values.redis.enabled }}
{{- printf "%s-redis-master" .Release.Name }}
{{- else }}
{{- .Values.externalRedis.host }}
{{- end }}
{{- end }}
