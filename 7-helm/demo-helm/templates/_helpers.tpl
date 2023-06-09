{{- define "metadata.labels" }}
  labels:
    generator: helm
    date: {{ now | htmlDate }}
{{- end }}
