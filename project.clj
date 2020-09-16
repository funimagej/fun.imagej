(defproject fun.imagej/fun.imagej "0.4.2-SNAPSHOT"
  :description "Functional Image Processing with ImageJ/FIJI"
  :url "https://github.com/funimagej/fun.imagel"
  :license {:name "Apache v2.0"
            :url "https://github.com/funimagej/fun.imagej/LICENSE"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [seesaw "1.4.4"]
                 [clj-random "0.1.8"]

                 [random-forests-clj "0.2.0"]

                 ; Java libs
                 ;[net.imglib2/imglib2 "b33186a" :exclusions [com.github.jnr/jffi]]
                 [net.imglib2/imglib2 "5.10.0" :exclusions [com.github.jnr/jffi]]
                 [net.imglib2/imglib2-algorithm "0.11.2"]
                 [net.imglib2/imglib2-ij "2.0.0-beta-46"]
                 [net.imglib2/imglib2-cache "1.0.0-beta-13"]
                 [net.imglib2/imglib2-roi "0.10.3"]
                 [net.imagej/imagej "2.1.0" :exclusions [com.github.jnr/jffi
                                                         com.github.jnr/jnr-x86asm
                                                         org.scijava/scripting-renjin]]
                 [net.imagej/imagej-legacy "0.37.4"]
                 [ome/bioformats_package "6.5.0" :exclusions [ch.qos.logback/logback-classic
                                                              ch.qos.logback:logback-core]]

                 [org.joml/joml "1.9.25"]
                 [graphics.scenery/scenery "954e766"]
                 [sc.iview/sciview "1312c73" :exclusions [com.github.jnr/jffi
                                                          com.github.jnr/jnr-x86asm
                                                          ch.qos.logback/logback-classic
                                                          org.scijava/scripting-renjin]]

                 [net.imagej/imagej-ops "0.45.5" :exclusions [org.joml/joml]]
                 [net.imagej/imagej-mesh "0.8.0"]
                 [net.imagej/imagej-mesh-io "0.1.2"]

                 ;[com.github.saalfeldlab/n5 "a3f0406"]
                 ;[com.github.saalfeldlab/n5-ij "a5517c8"]
                 ;[com.github.saalfeldlab/n5-imglib2 "2a211a3"]

                 [org.janelia.saalfeldlab/n5 "2.2.1"]
                 ;[com.github.saalfeldlab/n5-ij ""]
                 ;[org.org.janelia.saalfeldlab/n5-ij "0.0.2-SNAPSHOT"]
                 [org.janelia.saalfeldlab/n5-imglib2 "3.5.1"]

                 [org.ojalgo/ojalgo "48.1.0"]

                 [sc.fiji/Auto_Threshold "1.17.1"]

                 [org.scijava/scijava-common "2.83.3"]

                 [ch.qos.logback/logback-classic "1.2.3"]]


  :resource-paths ["src/main/resource"]
  :java-source-paths ["java"]
  :repositories [["scijava.public" "https://maven.scijava.org/content/groups/public"]
                 ["jitpack.io" "https://jitpack.io"]
                 ["saalfeld-lab-maven-repo" "https://saalfeldlab.github.io/maven"]]
  :deploy-repositories [["releases" {:url "https://maven.imagej.net/content/repositories/releases"
                                     ;; Select a GPG private key to use for
                                     ;; signing. (See "How to specify a user
                                     ;; ID" in GPG's manual.) GPG will
                                     ;; otherwise pick the first private key
                                     ;; it finds in your keyring.
                                     ;; Currently only works in :deploy-repositories
                                     ;; or as a top-level (global) setting.
                                     :username :env/CI_DEPLOY_USERNAME
                                     :password :env/CI_DEPLOY_PASSWORD
                                     :sign-releases false}]
                        ["snapshots" {:url "https://maven.imagej.net/content/repositories/snapshots"
                                      :username :env/CI_DEPLOY_USERNAME
                                      :password :env/CI_DEPLOY_PASSWORD
                                      :sign-releases false}]]
  ; Try to use lein parent when we can
  :plugins [[lein-exec "0.3.7"]]
  :jvm-opts ["-Xmx32g" "-server" "-Dscenery.Renderer=OpenGLRenderer"
             ;"-javaagent:/Users/kyle/.m2/repository/net/imagej/ij1-patcher/0.12.3/ij1-patcher-0.12.3.jar=init"
             #_"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:8000"])
