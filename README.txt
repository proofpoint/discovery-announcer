Server to monitor a URI and announce it into discovery.

Configure with:

service.foo.check.uri=http://localhost:8080/api/v1/version

Where "foo" is the service type to be announced. Also configure the usual
node.environment, node.pool, and service-inventory.uri.

This server will periodically issue GET requests to the supplied URI and,
when the monitored URL returns 200-series status codes, announce into
discovery as the configured service "foo".

Unless the announcement properties are explicitly configured, the announcement
will use the protocol and port of the check URI and the node IP (for http) or
hostname (for https).

Multiple service types and associated URIs may be configured.
