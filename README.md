[![Build Status](https://travis-ci.org/kszatan/gocd-b2-artifacts.svg?branch=master)](https://travis-ci.org/kszatan/gocd-b2-artifacts)

# gocd-b2-artifacts

Plugins for GoCD to publish, fetch and poll artifacts to/from Backblaze B2 storage.

## Requirements

Should work with [GoCD](https://gocd.org) >= 17.11.0.

## Overview

This project consists of 3 plugins that make it possible to integrate [GoCD](https://gocd.org) with the 
[Backblaze B2 Cloud Storage](https://www.backblaze.com/b2/cloud-storage.html):
* Publish plugin - pushes artifacts to B2.
* Material plugin - polls B2 for new artifacts.
* Fetch plugin - downloads artifacts from B2.

Publish plugin can be used standalone to push artifacts to a desired bucket and path prefix. Material and Fetch plugins
depend on each other. Material plugin only polls for the new packages (published artifacts) and does not download any 
files on its own. Fetch plugin has to be used in the same pipeline as Material plugin to download desired files. This 
follows GoCD design.
Please read [GoCD Package Material](https://docs.gocd.org/current/extension_points/package_repository_extension.html)
to get more information on package management in GoCD.

## Installation

1. Download the plugin(s) from [Releases](https://github.com/kszatan/gocd-b2-artifacts/releases).
2. Follow [plugin installation instructions](https://docs.gocd.org/current/extension_points/plugin_user_guide.html).

## Usage

### Publish plugin

Publish plugin is a simple task plugin and as such it needs some additional configuration to work. B2 credentials have 
to be set as environmental variables, either on a pipeline level, stage level or job level.

![Publish Plugin Configuration](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/publish-conf.png)

`B2_ACCOUNT_ID` and `B2_APPLICATION_KEY` should contain account ID and application key of your B2 account. Bucket name
is set for each publish task separately as described below.

Go to `Edit pipeline` view, choose a job, `Tasks` tab and then click `Add new task > Publish to B2`.

![Add Publish Task](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/publish-task.png)

Fill out the form and click `Save`.

![Publish Plugin Form](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/publish-form.png)

The plugin can upload files from multiple sources. Each source can be defined using wildcards and have its own
destination, that is, additional prefix when uploaded.

By default Publish plugin uploads artifacts using `PIPELINE_NAME/STAGE_NAME/JOB_NAME/PIPELINE_COUNTER.STAGE_COUNTER` 
prefix. For the pipeline visible in the above pictures this would mean for example `build-linux/build/build-app/10.2`
prefix, where `10` is the pipeline counter and `2` is the stage counter when this job was run. If you don't intend to 
use Material/Fetch plugins from this project you can override this behavior by setting a custom prefix in 
`Destination Prefix` field. Otherwise, leave it empty.

`Bucket name` is a name of an existing bucket in the account configured using `B2_ACCOUNT_ID` and `B2_APPLICATION_KEY` 
environmental variables. Optionally, you can set `GO_ARTIFACTS_B2_BUCKET` environmental variable. This field has 
precedence over the variable. 

### Material plugin

Configuration of Material plugin requires adding a package repository definition. Go to `Admin > Package Repositories`
and click `Add New Repository`. Fill out the form and click `CHECK CONNECTION` to check whether the plugin can connect
to the account and find the bucket. If everything is OK, click `Save`.

![Repository configuration](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/repo-conf.png)

With repository configured, go to your pipeline settings and tab `Materials`. Click `Add Material > Package`.

![Add Material](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/material-add.png)

Choose previously configured repository and click `Define new` if you haven't configured any packages yet. Fill out the
form with pipeline, stage and job names, which uniquely identify a job that pushes artifacts using Publish plugin. 
Optionally, click `CHECK PACKAGE` to check if the plugin can find it. In case you haven't pushed any artifacts at this
point you are going to get an error message, which you can ignore. Click `Save Package and Material` to finish.

![Material Form](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/material-form.png)

The material is now ready and will poll for new packages. When found, it will trigger the pipeline. Apart from doing 
just that, material plugin also sets very important environmental variables, with information containing repository 
name, package name, credentials and polled package version. This is needed in order for Fetch plugin to work properly.
You can see these variables in a job log.

### Fetch plugin

If there is a B2 material defined for a given pipeline you can use Fetch plugin to download its artifacts to a job's
workdir. Go to the job's settings, tab `Tasks`, and click `Add new task > Fetch from B2`.

![Add Fetch Task](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/fetch-add.png) 

Now fill out the form with the previously defined repository and package names. Optionally, provide destination path 
where artifacts should be downloaded. Click `Save` and you are good to go! :sparkles:

![Fetch Form](https://raw.githubusercontent.com/kszatan/gocd-b2-artifacts/1f24383a02bc711a46680f49813db620f1580d86/fetch-form.png)


## Bugs

Let me know if you find any.

## Credits

This project was inspired by [indix/gocd-s3-artifacts](https://github.com/indix/gocd-s3-artifacts). 