(ns fun.imagej.utils  
  (:use [fun.imagej core])
  (:require [clojure.string :as string]
            [clojure.pprint :as pprint]
            [fun.imagej.core :as ij]
            [clojure.java.io :as io]))

(defn directory-as-img-cache
  "Create a cache that uses a directory and the
  filesystems to back it. Customized for fun.imagej's
  image files.
  An img-cache is just a map of functions.
  There is an internal cache which has key's that map to
  filenames.
  Files are assumed to be tif"
  [directory]
  (let [cache (atom {})]
    {:has? (fn [key] (and (cache key)
                          (.exists (io/file (cache key)))))
     :get  (fn [key] (ij/open-img (get cache key)))
     :as-filename (fn [key] (str directory java.io.File/separator (name key) ".tif"))
     :write-return (fn [key update-fn]
                     (let [im (update-fn)]
                       (ij/save-img im (cache key))))})

(defn get-or-update!
  "Cache is an img-cache created with
  directory-as-img-cache
  key is a keyword
  update-fn returns an image that can be written to disk
    with ij/save-img. in this implementation the whole image will be
    loaded into memory. perhaps a later version could use imglib2-cache"
  [cache key update-fn]
  (if ((:has? cache) key)
    ((:get cache) key)
    ((:write-return cache) key (update-fn key))))
