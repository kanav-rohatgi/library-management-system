# Container releases

This is the release path for the local demo stack. GitHub Actions builds both images for Linux AMD64 and ARM64 when a version tag such as `v1.0.0` is pushed from a commit on `main`. Only tag a commit after its CI checks pass. The workflow publishes to GHCR and records both image digests in its summary. Release images include build provenance and a software bill of materials.

After the release workflow succeeds, copy its backend and frontend image references into your shell:

```bash
export LIBRARY_BACKEND_IMAGE='PASTE_BACKEND_RELEASE_REFERENCE'
export LIBRARY_FRONTEND_IMAGE='PASTE_FRONTEND_RELEASE_REFERENCE'
docker compose -f docker-compose.yml -f deploy/compose.images.yml up --no-build --pull always
```

Run that command from the repository root. Docker Desktop selects the appropriate image architecture. If the GHCR packages are private, sign in to `ghcr.io` using a token with package read access. The Compose override retains the demo stack's ports, configuration, and MySQL volume.

To stop the same stack and retain the database:

```bash
docker compose -f docker-compose.yml -f deploy/compose.images.yml down
```

## Docker Desktop build I/O errors

An error such as `commit failed: input/output error` at `WORKDIR` occurs while BuildKit writes the image layer. Check the Mac's free disk space and Docker Desktop’s disk usage and limit. Restart Docker Desktop before retrying.

```bash
df -h /
docker system df
docker buildx ls
docker compose build backend
docker compose up --build
```

If Docker Desktop restarts successfully and the write error persists, you can run `docker builder prune` to remove unused build cache. This can cause image layers to be downloaded or rebuilt again. For an alternative once a GHCR release exists, use the prebuilt-image command above. If Docker Desktop also reports I/O errors while pulling or starting those images, collect diagnostics under **Troubleshoot** in Docker Desktop.

## Deployment roadmap

| Stage | Deliverable |
| --- | --- |
| Pull requests | The existing Maven, image build, and Compose checks |
| Releases | Versioned multi-platform images, SBOMs, provenance, and deployable digests |
| Staging | Provision a Linux VM through infrastructure as code, set up HTTPS and secrets, run vulnerability scans, and smoke-test the deployment |
| Production | Promote the same image digests, require approval, and document rollback and database compatibility |
| Operations | Monitor uptime, errors, and capacity. Schedule MySQL backups and verify restores. |

Staging and production need a host, DNS, and environment-specific credentials. The mergeable release workflow requires only the repository's GitHub Actions token.
