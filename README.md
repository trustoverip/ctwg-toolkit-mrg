# Machine Readable Glossary (MRG) Generator

![build.yaml](https://github.com/trustoverip/ctwg-mrg-gen/actions/workflows/build.yaml/badge.svg)

## Overview

The Machine Readable Glossary Generator (MRG) tool is part of the Terminology Engine version 2 (
TEv2)
Toolbox developed by eSSIF-Lab and governed by the Trust Over IP (ToIP) Concepts and Terminology
Working Group (CTWG).
A detailed description of the tool, its purpose and related concepts can be found at
the [MR Glossary Generation page](https://essif-lab.github.io/framework/docs/tev2/spec-tools/mrgt)
whilst and
its [structure at this page](https://essif-lab.github.io/framework/docs/tev2/spec-files/mrg).

This README assumes the reader is familiar with these concepts and instead focuses on how someone
can download, install, and use the MRG.

### What does the MRG do?

The MRG helps terminology creators make a Machine Readable Glossary from a set of curated texts.
This glossary
is then the foundation to create and format additional content, e.g. human-readable glossaries, term
resolution links or widgets in documents and websites, etc.

### Who will use the MRG

The MRG will be used by terminology creators and curators and requires a number of terminology
artefacts
to be present, namely
the [curated texts](https://essif-lab.github.io/framework/docs/tev2/spec-files/ctext) themselves -
the MRG is not an authoring tool

- and
  a [Scope Administration File]( https://essif-lab.github.io/framework/docs/tev2/spec-files/saf) (
  SAF) as defined in the eSSIF documentation.

### How does the MRG work?

The MRG will run on a curator's machine in its own container and will connect to one or more GitHub
repositories where the curated files reside. The Scope Administration File (SAF) of the primary (or
local) scope
repository contains instructions as to how to create the MRG (e.g. which versions to use, which
terms to include, etc.)
and the MRG follows these instructions to build the glossary from the local terms and any terms from
remote scopes (i.e. other repositories) that the SAF specifies.

Full details of terminology construction can be found
at [the following page]( https://essif-lab.github.io/framework/docs/tev2/spec-tools/terminology-construction)

### Note

As of October 2022 the specification of the tool, term construction and other key concepts are still
under construction so this might change and the implementation and these instructions might also
need
to change with them.

## Before you begin

### Pre-requisites

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
  * The MRG tool runs in its own container on the curator's machine and there is a Docker image that
    the curator will need to download
  * This image will run in Docker and Docker Desktop provides a simple user interface to start and
    stop Docker containers

