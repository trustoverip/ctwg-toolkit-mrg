# Machine Readable Glossary (MRG) Generator

![build.yaml](https://github.com/trustoverip/ctwg-mrg-gen/actions/workflows/build.yaml/badge.svg)

## Overview

The Machine Readable Glossary Generator (MRG) tool is part of the Terminology Engine version 2 (
TEv2)
Toolbox developed by eSSIF-Lab and governed by the Trust Over IP (ToIP) Concepts and Terminology
Working Group (CTWG).
A detailed description of the tool, its purpose and related concepts can be found at
the [MR Glossary Generation page](https://essif-lab.github.io/framework/docs/tev2/spec-tools/mrgt)
whilst its [structure at this page](https://essif-lab.github.io/framework/docs/tev2/spec-files/mrg).

This README assumes the reader is familiar with these concepts and instead focuses on how someone
can download, install, and use the MRG.

### What does the MRG do?

The MRG helps terminology creators make a Machine Readable Glossary from a set of curated texts.
This glossary
is then the foundation to create and format additional content, e.g. human-readable glossaries, term
resolution links or widgets in documents and websites, etc.

### Who will use the MRG

The MRG will be used by terminology creators and curators and requires a number of terminology
artefacts to be present, namely
the [curated texts](https://essif-lab.github.io/framework/docs/tev2/spec-files/ctext) themselves -
the MRG is not an authoring tool and
a [Scope Administration File]( https://essif-lab.github.io/framework/docs/tev2/spec-files/saf) (SAF)
as defined in the eSSIF documentation.

### How does the MRG work?

The MRG will run on a curator's machine in its own container and will connect to one or more GitHub
repositories where the curated files reside. The Scope Administration File (SAF) of the primary (or
local) scope
repository contains instructions as to how to create the MRG (e.g. which versions to use, which
terms to include, etc.)
and the MRG follows these instructions to build the glossary from the local terms and any terms from
remote scopes (i.e. other repositories) that the SAF specifies.

Once run it will generate the MRG in directory the user selects on their local machine. This will
usually
be ````glossaries```` directory in the local clone of the scope they are currently editing, i.e. the
GitHub local directory,
e.g. ````/Users/foo/tev2/glossaries```` or ````C:/Users/foo/work/tev2/glossaries````

Full details of terminology construction can be found
at [the following page]( https://essif-lab.github.io/framework/docs/tev2/spec-tools/terminology-construction)

### Note

As of October 2022 the specification of the tool, term construction and other key concepts are still
under construction so this might change the implementation and these instructions might also
need to change with them.

## Before you begin

### Technical pre-requisites

* An account on [GitHub](https://github.com/)
  * Terminologies are developed and shared on GitHub
  * The MRG tool uses the GitHub APIs to fetch terminology artefacts, and valid credentials are
    needed in order authenticate with GitHub
*
A [personal access token on GitHub](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
  * The MRG tool uses the GitHub user and a personal access token to authenticate with GitHub
  * This ensures that the curator can benefit from
    the [higher GitHub API rate limits](https://docs.github.com/en/developers/apps/building-github-apps/rate-limits-for-github-apps)
      * Anonymous access is limited to only 50 requests per hour which for scopes with more than 50
        files would make generation impossible
  * [Docker](https://www.docker.com/products/docker-desktop/)
      * The MRG tool runs in its own container on the curator's machine and there is a Docker image
        that
        the curator will need to download
      * This image will run in Docker and Docker Desktop provides a simple user interface to start
        and stop Docker containers

## Generating a machine-readable glossary

In order to generate the MRG you will have a scope repository on GitHub containing the Scope
Administration File (SAF) and the curated texts. If terms are being imported from other repositories then these external scopes will have been defined in the SAF and the appropriate versions selected. See the links to the eSSIf-lab.

### 1. Start Docker Desktop

You should find this in Applications (Mac) or the Start Menu (Windows) depending on how you
installed the software.

It might take a minute or two to start but when the whale turns green then it has started and is
ready to use.

### 2. Pull the MRG Generator from Trustoverip GitHub packages

Browse
the [Trust Over IP MRG Packages](https://github.com/orgs/trustoverip/packages/container/package/ctwg-mrg-gen) to find the latest version of the CTWG MRG Generator.

![ToIP CTWG MRG Packages page](./docs/toip-github-package.png?raw=true "ToIP CTWG MRG Packages")

Copy the docker pull command on this page or if you prefer use the digest as per the command below
to download the correct version. You may need to change the digest to match that of the version you need.

Paste this command in to a Terminal window (Mac) or a command prompt (Windows)

````docker pull ghcr.io/trustoverip/ctwg-mrg-gen@sha256:f9aa075d8f9083df86a28e133f6d9205b26b5fd928fad47b48b30700ac3730de````

This will download a new image to your Docker Desktop as below.

![MRG Generator Image in Docker Desktop](./docs/docker-image.png?raw=true "MRG Generator Image in Docker Desktop")

### 3. Run the MRG Generator

* Hover over the Docker image in Docker Desktop and click the ````Run```` button on the right-hand
  side
* A smaller window will appear. Don't click run yet but instead select ````Optional Settings````
* Make sure you have your GitHub Personal Access Token to hand and fill out the settings as below (
  substituting your details where needed)
    * Choose a local port to map the container to. This example uses 8083
    * Use the selector on the Volume to select your local directory where the MRG will be written
    * Enter your GitHub username against the gh_user environment variable
    * Enter your GitHub personal access token against the gh_token environment variable

![Docker Run Optional Settings](./docs/docker-optional.png?raw=true "Docker Run Optional Settings")

* Click Run

This will start up a Docker container and when you click ````Containers```` on your Docker Desktop
you should see something like:

![Running MRG Container in Docker Desktop](./docs/docker-running.png?raw=true "Running MRG Container in Docker Desktop")

### 4. Check the MRG log output in Docker Desktop

Some useful logging is output to a console and this can be viewed in Docker Desktop.

* From the ````Containers```` screen in Docker Desktop screen that shows your Click the three
  vertical dots on the right-hand side by the CTWG container
    * This has a tooltip saying ````Show Container Actions````
* Click ````View Details````

This shows the log output from the running container. You might see output still being produced but
when you get a screen similar to the one below that contains
````Started MRGWebApp ... ```` then the container is up and running

![Log output showing MRG started](./docs/mrg-log-output-initial.png?raw=true "Log output showing MRG started")

### 5. Connect to the MRG Generator web server via a browser

This has been tested using Chrome, but should work with most modern browsers

* Navigate to ````http://localhost:8083/ctwg/mrg```` in your browser

![MRG form in Chrome browser](./docs/mrg-form.png?raw=true "MRG form in Chrome browser")

There are three fields to fill out:

1. Scope directory location

* This is the remote GitHub directory containing the curated texts (terms) and should follow the
  conventions listed in the eSSIF-Lab rules

2. Scope Admin File (SAF)

* This is the name (not location) of the SAF file in the remote repository
* It will most likely be ````saf.yaml```` as shown in the diagram

3. Scope version tag

* The version of the glossary that should be created
* Versions are defined in the Scope Administration File see
  the [eSSIF documentation]( https://essif-lab.github.io/framework/docs/tev2/spec-files/saf)

Once these are filled out click the ````Generate```` button

### 6. View the MRG generation at work

Depending on the number of files and external calls this may take a while to complete. You can watch
the progress in the Docker Desktop log output (see previous steps;)

![MRG log output with completion messages](./docs/mrg-log-output-full.png?raw=true "MRG log output with completion messages")

When the final step (6/6) is complete then you should see your generated MRG in the browser window.

![MRG web output](./docs/mrg-web-output.png?raw=true "MRG web output")

The same file will have been written to the local directory you specified in Step 3

![MRG file output](./docs/mrg-file-output.png?raw=true "MRG file output")

The latter can be added to GitHub and then pushed in to the remote repository.