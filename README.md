dash4twitter
============

Authors: **Joseph Perez** (jdperez@utexas.edu) and **Stephen Pryor** (spryor02@utexas.edu)

dash4twitter is a simple dashboard application for twitter analysis. It is designed for easy modification for visualization and filtered streaming functionality. 

## Requirements

* Version 2.1.1 of the Play Framework (http://www.playframework.com/)
* Version 2.10.1 of Scala (http://www.scala-lang.org/)

## Setting up twitter4j
To use dash4twitter, you need to have authentication credentials from twitter. Make on application on using the twitter developers page and place your credentials in a file called `twitter4j.properties`

    oauth.consumerKey=`your consumer key`
    oauth.consumerSecret=`your consumer secret`
    oauth.accessToken=`your access token`
    oauth.accessTokenSecret=`your access token secret`
          
## Launching the Dashboard with Play
Go to the directory you cloned dash4twitter in. Use the following command:

    $ play run

Then open the file `app/view/index.html` file in the browser of your choice.
