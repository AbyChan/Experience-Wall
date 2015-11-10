(ns app.core
  (:gen-class)
  (:use markdown.core
        [clojure.string :only (join split)]
        [io.aviso.ansi]
        [io.aviso.logging])
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [cheshire.core :refer :all]))


(def default-config-file "_config.json")


(defn show-banner []
  (println "EXPERIENCE WALL"))

(defn show-help []
  (show-banner)
  (println (join "\n"
                 ["Usage:"
                  "Ewall [command]"])))

(defn read-default-config []
  (parse-string (slurp (io/resource
                        default-config-file)) true))

(def default-config (read-default-config))


(defn current-path []
  (str fs/*cwd*))

(defn join-path [parent path]
  (str parent "/" path))

(defn join-path-cwd [path]
  (print path)
  (println (join-path (current-path) path))
  (join-path (current-path) path))

(defn new-ewall
  [rest]
  (if (< (count rest) 1)
    (do
      (fs/mkdir (join-path-cwd (:source_dir default-config)))
      (fs/mkdir (join-path-cwd (:public_dir default-config)))
      (spit (join-path-cwd default-config-file) (slurp (io/resource
                                                        default-config-file))))
    (do
      ())))

(defn new-experience
  [rest]
  (println))

(defn start-server
  [rest]
  (println))

(defn release-experience
  [rest]
  (println))

(defn -main
  [& args]
  (if (< (count args) 1)
    (show-help)
    (let [command (first args)]
      (cond
        (= command "init") (new-ewall (rest args))
        (= command "new") (new-experience (rest args))
        (= command "release") (release-experience (rest args))
        (= command "serve") (start-server (rest args))
        (= command "help") (show-help)
            :else (show-help)))))
