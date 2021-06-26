## v0.3.0

* Setup code per wzgp updated api, vm zones in use added
* k8s deployment config fixed and updated
* Logging enhancer added
* Fixed separate service account problem, we don't need a separate service account on VM, I've added
  k8s service account into firebase project's IAM permission, now it should work
* Fixed jib configuration per newest version

## v0.3.2

* fixed logback so that project's root level could be changed
* fixes in deployment

## v0.3.3

* Upgrade runner machine to 4 cpu
* fixed an endpoint in openapi

##v0.3.4

* Changed support query email to support@zy..
* runner, esdb are now not using secrets locally, their production beans are separated.
* changed nginx config to allow 5mins of timeout for long running create build requests
* Session assets buckets are now being taken from props file, separate file for non-prod contains their dev versions
* configured separate firebase project for dev env

## v0.3.5

* Upgraded zwl to 0.4.2
* always sending true to IE cache/history clear flag, this is done from here because we want this
  true in all environment, all clients.

## v0.3.6

* Added controller for invitation request

## v0.3.7

Minor enhancements

### Enhancements

1. Added discourse sso support.
2. Upgraded `zwl` to 0.4.3.

## v0.3.8

Bug fixes

### Bug fixes

1. Fixed a bug in discourse sso