(ns fun.imagej.mastodon.demo
  (:require [fun.imagej.core :as ij])
  (:import (org.scijava.command CommandService)
           (org.mastodon.revised.mamut Mastodon)))

(defn new-mastodon
  "Create a new instance of mastodon"
  []
  (let [ctxt (.getContext (ij/get-ij))
        command (.service ctxt CommandService)
        mastodon (.run command
                       Mastodon
                       true
                       (into-array Object
                                   []))]
    mastodon))



