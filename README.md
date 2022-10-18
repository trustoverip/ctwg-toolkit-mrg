# Machine Readable Glossary (MRG) Generator

![build.yaml](https://github.com/trustoverip/ctwg-mrg-gen/actions/workflows/build.yaml/badge.svg)

## 1. Overview {#1}

The Machine Readable Glossary Generator (MRG) tool is part of the [Terminology Engine version 2 (
TEv2)](https://essif-lab.github.io/framework/docs/tev2/tev2-overview)
Toolbox developed by eSSIF-Lab and governed by the Trust Over IP (ToIP) [Concepts and Terminology
Working Group (CTWG)](https://wiki.trustoverip.org/pages/viewpage.action?pageId=65700).
A detailed description of the tool, its purpose and related concepts can be found at
the [MR Glossary Generation page](https://essif-lab.github.io/framework/docs/tev2/spec-tools/mrgt)
whilst its [structure at this page](https://essif-lab.github.io/framework/docs/tev2/spec-files/mrg).

This README assumes the reader is familiar with these concepts. It focuses on how someone can
download, install, and use the MRG.

### 1.1 What does the MRG generator do? {#1.1}

The MRG helps terminology creators make a Machine Readable Glossary from a set of curated texts
that are curated in a particular scope, and a selection of terms curated in other scopes.
See the [TEv2 architecture](https://essif-lab.github.io/framework/docs/tev2/overview/tev2-architecture)
for its position in the toolbox.

The MRG generator is NOT an authoring tool. Authoring and curating terms is authoring and curating the
[curated texts](https://essif-lab.github.io/framework/docs/tev2/spec-files/ctext) that the MRG generator
uses as input for creating an MRG.

An MRG that is created with this tool is typically used as the foundation
to create and format additional content, e.g. human-readable glossaries,
term resolution links or widgets in documents and websites, etc.

### 1.2 Who will use the MRG generator {#1.2}

The MRG generator will be used by terminology creators and curators to generate an MRG.
It can also be used in a CD/CI pipe to automatically generate an MRG as part of a GitHub action or similar.

### 1.3 What inputs does the MRG generator need {#1.3}

For MRG generation to work, the following artefacts need to be present:
- The [Scope Administration File (SAF)](https://essif-lab.github.io/framework/docs/tev2/spec-files/saf);
- Access to (already existing) [MRGs](https://essif-lab.github.io/framework/docs/tev2/spec-files/mrg) insofar as they contain terms that are to be included in the MRG that the generator creates;
- The [curated texts](https://essif-lab.github.io/framework/docs/tev2/spec-files/ctext) that document the terms (or other artifacts) that are to be included in the MRG that the generator creates).

### 1.4 How does the MRG work? {#1.4}

The MRG will run on a curator's machine in its own docker container.
It will connect to one or more GitHub repositories where the curated files reside,
and to the repositories where MRGs reside from which terms need to be imported.

The Scope Administration File (SAF) of the primary (or local) scope repository contains
the instructions concerning how to create the MRG (e.g. which versions to use,
which terms to include, etc.). The MRG generator follows these instructions to build
an MRG from the local terms and any terms from remote scopes (i.e. other repositories)
that the SAF specifies.

Once run it will generate the MRG in directory the user selects on their local machine.
This will usually be the ````glossaries```` directory in the local clone of the scope
that the curator is currently editing, i.e. the GitHub local directory,
e.g. ````/Users/foo/tev2/glossaries```` or ````C:/Users/foo/work/tev2/glossaries````

Full details of terminology construction can be found
at [the following page]( https://essif-lab.github.io/framework/docs/tev2/spec-tools/terminology-construction)

### 1.5 Note {#1.5}

As of October 2022 the specification of the tool, term construction and other key concepts are still
under construction so this might change the implementation and these instructions might also
need to change with them.

## 2. Before you begin - technical pre-requisites {#2}

There are some things you need to do to prepare yourself for generating MRGs:
1. Ensure that the generator can access the various GitHub repositories that it needs;
2. Ensure that you can run Docker containers;
3. Ensure that you have the (most recent) version of the MRG generator tool as a docker image.

### 2.1 Enable GitHub Access {#2.1}

You need to work with [GitHub](https://github.com/), as terminologies are developed and shared (curated) there. Also, the MRG tool uses the GitHub APIs to fetch terminology artefacts. So you will need
* a GItHub account, so you can get access to the various repositories;
* a GitHub personal access token, which ensures you can benefit from the [higher GitHub API rate limits](https://docs.github.com/en/developers/apps/building-github-apps/rate-limits-for-github-apps) (anonymous access is limited to only 50 requests per hour which for scopes with more than 50 files would make generation impossible)

If you don't have one, you can [sign up for a GitHub account](https://docs.github.com/en/get-started/signing-up-for-github/signing-up-for-a-new-github-account). You can use the simiplest (free) kind. You will need to supply your username to the MRG generator so it can use this account to access the GitHub API in your name.

The simplest way for you to get a [personal access token on GitHub](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) is by using this [direct link](https://github.com/settings/tokens). You will need to supply the token to the MRG generator so it can access the GitHub API in your name.

You can generate many such tokens, but you only need one for the generator. When creating or refreshing the token for the MRG generator, choose the following access settings:

![Personal Access Token settings](./docs/github-pat-settings.png?raw=true "Personl Access Token settings")

You may want to save the token for later (re)use. However, you can also always generate/refresh the token if that is needed (GitHub will notify you a few days before it expires).

### 2.2 Enable Running Docker Containers {#2.2}

In order to locally run the MRG generator, you need to be able to run docker containers.
Thus you need to install [Docker Desktop](https://www.docker.com/products/docker-desktop/) on your local machine.
Make sure you have a relatively recent version - older versions may not work the way we describe things here.

You should find this in Applications (Mac) or the Start Menu (Windows) depending on how you installed the software. It might take a minute or two to start but when the whale turns green then it has started and is ready to use.

### 2.3 Getting the MRG generator as a Docker image {#2.3}

In order to locally run the MRG generator, you need the (latest, most recent) Docker image that contains the MRG generator, which you can then run in a container. First, you browse the [Trust Over IP MRG Packages](https://github.com/orgs/trustoverip/packages/container/package/ctwg-mrg-gen) to find the latest version of the CTWG MRG Generator.

![ToIP CTWG MRG Packages page](./docs/toip-github-package.png?raw=true "ToIP CTWG MRG Packages")

Copy the docker pull command on this page to download the correct version (the version number may differ from what is shown in the above figure).

Paste this command in to a Terminal window (Mac) or a command prompt (Windows)

````docker pull ghcr.io/trustoverip/ctwg-mrg-gen:latest````

This will download a new image to your Docker Desktop as below.

![MRG Generator Image in Docker Desktop](./docs/docker-image.png?raw=true "MRG Generator Image in Docker Desktop")

## 3. Generating a machine-readable glossary {#3}

An MRG is generated within the context of a scope-directory that resides in a GitHub repository. The scope-directory is the directory that contains the Scope Administration File (SAF) and the curated texts. If terms are being imported from other scope directories (in the same, or other repositories), then these external scopes will have been defined in the SAF and the appropriate versions selected. Further explanations can be found [here](https://essif-lab.github.io/framework/docs/tev2/overview/tev2-terminology-curation).

Generating an MRG consists of:
1. Starting the MRG generator in a Docker container;
2. Start your webbrowser and instruct the MRG generator to create an MRG
3. Obtain/view the MRG output.

When things go wrong, you can check the various logs.

### 3.1 Starting the MRG generator in a Docker container {#3.1}

You must have completed the prerequisites, and have started the Docker Desktop and downloaded the MRG generator docker image (instructions are above). Then complete the following steps to start the MRG generator in its docker container. Then, it will run as a web service that you can use/call multiple times, e.g. to generate multiple MRGs, as follows:

* Hover over the Docker image in Docker Desktop and click the ````Run```` button on the right-hand
  side. A smaller window will appear. Don't click run yet but instead select ````Optional Settings````

![Docker Run Optional Settings Initial](./docs/docker-optional-initial.png?raw=true "Docker Run Optional Settings Initial")

* Now another window will appear that contains fields you need to fill in:

![Docker Run Optional Settings](./docs/docker-optional.png?raw=true "Docker Run Optional Settings")

  * under 'Optional settings', you type the name of the container as you like it, e.g. `ctwg-mrg`.
  * under 'Ports`, you type the port number of where you can access the tool on localhost, e.g. `8083`. This means that you can later browse to `localhost:8083/ctwg/mrg` to make the tool run.
  * under 'Volumes', there are rows that consist of two fields, the left one specifying a directory on your local machine, and the right one specifying a directory on the (virtual) machine in the docker container. The idea is that when the MRG generator writes the MRG in the directory of the docker container, it will be automatically transferred to the local directory, so it becomes available for you to do with as you like. So here is how you fill in the fields
    * the left field ('Host path') specifies a directory on your local machine, e.g. `C:\git\my-repodir\glossaries`
    * the right field ('Container path') MUST contain the text `/glossaries`, as that is the path in the container where the MRG generator will put the generated MRG.
  * under 'Environment variables, you see two rows with fields `Variable` and `Value`.
    * in the first field (with Variable=`gh_user`), you enter your GitHub username (e.g.: `RieksJ`, or `sih`) in the `Value` field.
    * in the second field (with Variable=`gh_token`), you enter your GitHub access token (something like `ghp_v3fSgDIjlsXYZncjEzDQ1bLnwdl2YJOaF` (see the section [Enable GitHub Access](#2.1) above on how to get such a token if you need one)

* Click Run

This will start up a Docker container and when you click ````Containers```` on your Docker Desktop and you should see something like:

![Running MRG Container in Docker Desktop](./docs/docker-running.png?raw=true "Running MRG Container in Docker Desktop")

Depending on how much of the required software needs to be, or has already been downloaded, and also depending on the speed of your Internet connection, it may take anything from 15 seconds to a minute for the generator to be ready. *It is important to check the generator is ready before accessing it*.

### 3.2 Start your webbrowser and instruct the MRG generator to create an MRG {#3.2}

This has been tested using Chrome, but should work with most modern browsers

* Navigate to ````http://localhost:8083/ctwg/mrg```` in your browser. Note that it's not just localhost - you need to specify the complete path.

![MRG form in Chrome browser](./docs/mrg-form.png?raw=true "MRG form in Chrome browser")

There are three fields to fill out:

1. **Scope directory location**
  This is the URL at which the scope directory (scopedir) is located; it is typically a directory in a (remote!) GitHub repository. This directory must contain the SAF of the scope you want to generate an MRG for. It must also contain the so-called `curatedir` that contains the curated texts (terms). It would typically be something like `https://github.com/essif-lab/framework/tree/master/docs/tev2`.

2. **Scope Administration File (SAF)**
  This is the filename (not the location) of the SAF that is located in the scopedir. It would typically be called `saf.yamal` (as shown in the diagram).

3. **Scope version tag**
  This is the tag (name) of the glossary that should be generated. It must have been [defined in the SAF](https://essif-lab.github.io/framework/docs/tev2/spec-files/saf#versions). Typical values of it could be `latest`, or `v3.1` or so.

Once these are filled out click the ````Generate```` button
### 3.3 Obtain/view the MRG output {#3.3}

Generation of an MRG takes a bit of time, but not all that long. If it takes too long, you can watch the progress in the Docker Desktop log (see [next step]{#3.4}).

When generation is complete, your browser will show the file that has been generated:

![MRG web output](./docs/mrg-web-output.png?raw=true "MRG web output")

The same file will have been written to the local directory that you specified in the `Host path` field (in the step where you started the MRG generator in a container):

![MRG file output](./docs/mrg-file-output.png?raw=true "MRG file output")

The latter can be added to GitHub and then pushed in to the remote repository.

### 3.4 Check the MRG log output in Docker Desktop {#3.4}

Some useful logging is output to a console and this can be viewed in Docker Desktop.

* From the ````Containers```` screen in Docker Desktop, click on the three vertical dots on the right-hand side by the CTWG container. It has a tooltip saying ````Show container actions````
* Click ````View Details````

This shows the log output from the running container. You might see output still being produced but
when you get a screen similar to the one below that contains
````Started MRGWebApp ... ```` then the container is up and running

![Log output showing MRG started](./docs/mrg-log-output-initial.png?raw=true "Log output showing MRG started")

After the generation of an MRG is complete (which may take a while, your log would look something like this:

![MRG log output with completion messages](./docs/mrg-log-output-full.png?raw=true "MRG log output with completion messages")
