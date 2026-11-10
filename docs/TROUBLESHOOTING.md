---
summary: "Troubleshooting guide for ViewMD rendering, diagrams, and custom CSS"
triggers: [troubleshooting, blank preview, custom CSS error, diagram failure]
---

# ViewMD Troubleshooting

Consult this guide if a document fails to render as expected or if a custom stylesheet is not applied.

| Symptom | Likely Cause | Solution |
|---|---|---|
| Preview is blank | JCEF browser unavailable in environment | Verify that your IDE runtime is a JetBrains Runtime with JCEF enabled. Use "Open in Browser" as a fallback. |
| Custom CSS not applied | File not found or empty | Check `Settings > Tools > ViewMD` diagnostics. Verify whether the file is empty or if path resolution failed. |
| Relative path doesn't resolve | Project base path missing | Specify an absolute path or use `~/...` for stylesheets outside your repository. |
| Diagram displays error box | Invalid diagram syntax | Click the error card to inspect the line number and raw code fence source. |
| Table does not scroll horizontally | Table fits inside current viewport | Tables only scroll when column content exceeds the container width (`width: max-content`). |
