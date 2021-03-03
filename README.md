# ws-web

A simple cluster-communication example for teaching purposes.

Exposes a web server on port `36677` that prints host information and requests host information
from other configured hosts.

## Configuring hosts

Upon running the container, supply a comma-separated list of hosts to contact for collecting
information. Each host will be requested on port `36677` as well.

```bash
docker run ws-web ws-web-node1,ws-web-node-2
```

## Endpoints


- `/` prints information about the current host and other configured hosts
- `/single` prints information about the current host only