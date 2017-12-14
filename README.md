# FunImageJ

[![Build Status](https://travis-ci.org/funimage/funimage.svg?branch=master)](https://travis-ci.org/funimage/funimage)

---

FunImageJ is a Lisp/Clojure framework for scientific image processing built upon the ImageJ software ecosystem. The framework provides a natural functional-style for programming, while accounting for the performance requirements necessary in big data processing commonly encountered in biological image analysis.

Note to functional programming folks: many FunImageJ functions treat data as mutable. Images are large and take up lots of memory, we try not to duplicate data unless necessary. If you want to do something non-destructively, then you may need to explicitly use `copy` functions.

[API Documentation](https://kephale.github.io/fun.imagej/)

Example code for both standalone and Fiji usage is provided in the [*test* directory](https://github.com/kephale/fun.imagej/tree/master/test/fun/imagej/test) of this repository.  

---

## Getting started

### IntelliJ

1. Install [Cursive](https://cursive-ide.com/userguide/). 
2. Edit code as you usually would. Look at `fun.imagej.test.presentations.dd2017` to start.

### Eclipse

1. Install [Counterclockwise](http://doc.ccw-ide.org/). You can find it in the Eclipse marketplace.
2. Edit code as you usually would. Look at `fun.imagej.test.presentations.dd2017` to start.

### CLI

1. Install a build manager: [Leiningen](https://leiningen.org/) (default in these examples) or [boot](https://github.com/boot-clj/boot)
- If you are an Emacs user, then you might want to install [CIDER](https://github.com/clojure-emacs/cider)
2. `lein repl` will start up a Read-Eval-Print-Loop where you can interactively code
3. `lein run -m fun.imagej.test.presentations.dd2017` will launch a demo

### Introductory Clojure resources:
    
- [Introduction to Clojure syntax](https://clojure.org/guides/learn/syntax)
- [A basic tutorial](http://clojure-doc.org/articles/tutorials/introduction.html)
- [Grimoire interactive API lookup](https://www.conj.io/)
- [Clojure scripting in ImageJ](http://imagej.net/Clojure_Scripting) [Note: this resource is discouraged for 99.9% of users]

---

## Usage within ImageJ:

Add an update site like you would usually (see http://fiji.sc/List_of_update_sites)   

where the update site is: http://sites.imagej.net/FunImageJ/  

---

## Usage within Clojure projects:

Add the following repositories to your `project.clj`

```
  :repositories [["imagej-releases" "http://maven.imagej.net/content/repositories/releases/"]
                 ["imagej-snapshots" "http://maven.imagej.net/content/repositories/snapshots/"]]

```                 

Then add the fun.imagej dependency:

```
[fun.imagej/fun.imagej "0.2.4"]
```


---                 


License:

Apache V2.0

Copyright 2014-2017 Kyle Harrington.
