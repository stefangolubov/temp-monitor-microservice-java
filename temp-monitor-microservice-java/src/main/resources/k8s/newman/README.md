# Newman Collection Automation – Kubernetes Quick Start

This guide explains how to run an automated Newman (Postman) collection job in Kubernetes, and how to view the results via a web-based report viewer.

---

## Prerequisites

- Your Postman collection(s) are present on your local machine, e.g.:
    - `D:\DEV\Postman\collection.json`
- The `reports-pvc` Persistent Volume Claim is available in your cluster.
- In order the generated reports to be easily accessible, an Nginx server is preconfigured that serves the reports written to the reports-pvc (on default namespace).

---

## 1. Run the Newman Job

This job runs your Postman collection with Newman and writes JSON/JUnit reports to the shared PVC.

```sh
kubectl apply -f newman-collection-job.yaml
```

You can monitor the job with:

```sh
kubectl get pods -w
kubectl logs -f <newman-collection-job-pod-name>
```

---

## 6. View the Newman Reports

After the Newman job completes, reports will appear in the `/newman` directory served by the report viewer.

- Open: `http://<your-node-ip>:30080/newman/`
    - Find the most recent `.json` or `.xml` files (`tmmj-newman-report-<timestamp>.json`, etc.)

---

## 7. (Optional) Cleanup

To remove test resources:

```sh
kubectl delete job newman-collection-job
```

---

## Notes

- If you update your Postman collection, re-run the Newman job.
- If you need to mount the Newman collection from a different path, update the `newman-collection-job.yaml` volume definition and container path.
- You can run multiple Newman jobs; each will write a timestamped report to the shared PVC, visible in the report viewer.
- The same report viewer can be used for both ZAP and Newman jobs, as long as they both use the same reports PVC.

---