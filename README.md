# Preservica Schemas

This contains the custom schema definitions, stylesheets, index definitions and templates used with Preservica.

## Deploying to Preservica
The `Lambda.scala` class is packaged into a jar with all of its dependencies and is deployed to a Lambda within the VPC. 
This lambda is then run which takes all the files from each directory and uses the corresponding API call to upload them to Preservica.

Preservica doesn't allow you to update documents so this script deletes them first and then recreates them.

This script uses our own Preservica Scala client for all calls. 
