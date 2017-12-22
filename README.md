# FunImageJ

[![Build Status](https://travis-ci.org/funimage/funimage.svg?branch=master)](https://travis-ci.org/funimage/funimage)

---

FunImageJ is a Lisp/Clojure framework for scientific image processing built upon the ImageJ software ecosystem. The framework provides a natural functional-style for programming, while accounting for the performance requirements necessary in big data processing commonly encountered in biological image analysis.

Note to functional programming folks: many FunImageJ functions treat data as mutable. Images are large and take up lots of memory, we try not to duplicate data unless necessary. If you want to do something non-destructively, then you may need to explicitly use `copy` functions.

[API Documentation](https://kephale.github.io/fun.imagej/)

Example code for both standalone and Fiji usage is provided in the [*test* directory](https://github.com/kephale/fun.imagej/tree/master/test/fun/imagej/test) of this repository.
  
---

## Citing:

Kyle I S Harrington, Curtis T Rueden, Kevin W Eliceiri; FunImageJ: a Lisp framework for scientific image processing, Bioinformatics, btx710, https://doi.org/10.1093/bioinformatics/btx710

---

## Usage within ImageJ:

Add an update site like you would usually (see http://fiji.sc/List_of_update_sites)   

where the update site is: http://sites.imagej.net/FunImageJ/  

---

## Usage within Clojure projects:

Add the following repositories to your `project.clj`

```
  :repositories [["imagej-releases"       
                 "http://maven.imagej.net/content/repositories/releases/"]
                 ["imagej-snapshots" "http://maven.imagej.net/content/repositories/snapshots/"]]

```                 

Then add the fun.imagej dependency:

```
[fun.imagej/fun.imagej "0.2.1"]
```


---                 


License:

Apache V2.0

Copyright 2014-2017 Kyle Harrington.
