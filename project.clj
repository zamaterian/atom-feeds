(defproject zamaterian/atom-feeds "1.0.0-SNAPSHOT"
  :description "Rest api to create atom entries, and publish atom feeds"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.1"]
                 [ring/ring-core "0.3.3" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "0.3.3" :exclusions [javax.servlet/servlet-api]]
                 [ring-json-params "0.1.1"]
                 [clj-json "0.3.1"]
                 [ring-common "1.0.0-SNAPSHOT"]
                 [clojure-config "1.0.0-SNAPSHOT"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                                  javax.jms/jms
                                                  com.sun.jdmk/jmxtools
                                                  com.sun.jmx/jmxri]]]
  :dev-dependencies
  [[uk.org.alienscience/leiningen-war "0.0.9"]
   [org.apache.derby/derby "10.6.1.0"]
   [ring/ring-jetty-adapter "0.3.3"]
   [ring/ring-devel "0.3.3"]
   [autodoc "0.7.1" :exclusions [org.clojure/clojure-contrib]]
   [com.stuartsierra/lazytest "1.1.2"]
   [lein-clojars "0.6.0"]
   [lein-lazytest "1.0.1"]
   [javax.servlet/servlet-api "2.5"]]
  :namespaces [feeds.servlet]
  :repl-init-script "init.clj" ; init.cjl  is not under version control. se sample-init.clj
  :web-content "public"
  :lazytest-path ["src" "test/unit"]
  :autodoc {
        :name "Atom Feeds"
        :description "Create atom entries, and publish atom feeds"
        :output-path "public/autodoc"}
  )
