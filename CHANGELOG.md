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

## v0.3.9

Enhancements

### Enhancements

1. Beta invitation is no longer supported.
2. Supporting addition of new users without email confirmation.
3. Changed transactions email sender to support email.
4. Email sender name will have a name going forward.
5. New users can be added using email/pwd or identity provider.
6. Only one build from IDE at a time requirement no longer applies.

## v0.3.10

Enhancements

### Enhancements

1. Upgraded `zwl` to 0.4.5.
2. Email verification is now checking whether current user own verification email.
3. New user checks that team member creates account with same email.

## v0.3.11

Enhancements

### Enhancements

1. Upgraded `zwl` to 0.6.0.

## v0.3.12

Enhancements

### Enhancements

1. Added more plans to `PlanEnum`.

## v0.3.13

Bug fixes

### Bug fixes

1. All files were loading, not taking into account the selected project.

## v0.3.14

Enhancements

### Enhancements

1. Added outomated domains as allowed clients

## v0.3.15

Enhancements

### Enhancements

1. Changed emails to outomated

## v0.3.16

Enhancements

### Enhancements

1. Added new plans

## v0.3.17

Bug fixes

### Bug fixes

1. Fixing front end url

## v0.3.18

Bug fixes

### Bug fixes

1. Fixed a bug in nginx to allow bigger file uploads

## v0.3.19

Bug fixes

### Bug fixes

1. Fixed a bug in fileWithTests to allow files having no tests.

## v0.3.20

Enhancements

### Enhancements

1. Added team invite.
2. Upgraded zwl to 0.6.3

## v0.3.21

Enhancements

### Enhancements

1. Added api key api

## v0.3.22

Enhancements

### Enhancements

1. UI support for shots and driver logs preferences 

## v0.3.23

Enhancements

### Enhancements

1. Creating a new email preference record for every new user.

## v0.3.24

Enhancements

### Enhancements

1. Added mobile emulation in build capability.
2. Bumped zwl to 0.6.4

## v0.3.25

Enhancements

### Enhancements

1. Added new api endpoint for getting completed version status
2. Incorporate url_on_error in BuildStatus

## v0.3.26

Enhancements

### Enhancements

1. Dropped one of the vm zone and kept just one for now. This is done for keeping the cost low so that
   if one user is currently developing in the app, we keep only one VM running. When starting a new 
   VM, the other zone will still be reattempted in provisioner if it fails in the given zone.

## v0.3.27

Enhancements

### Enhancements

1. Bumped zwl to 0.6.5
