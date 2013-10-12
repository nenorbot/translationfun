(defproject translationfun "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :main translationfun.main
  :repositories {
                 "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"
                 }
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [ring-server "0.2.8"]
                 [selmer "0.4.2"]
		 [com.memetix/microsoft-translator-java-api "0.6.2"]
                 [domina "1.0.2-SNAPSHOT"]
                 [crate "0.2.4"]
                 [http-kit "2.1.10"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]]
  :uberjar-name "translationfun-standalone.jar"
  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.8.5"]
            [lein-cljsbuild "0.3.2"]]
  :cljsbuild {:builds
              [{:source-paths ["src-cljs/translationfun"]
                :compiler
                {:optimizations :advanced
                 :pretty-print false
                 :output-to "resources/public/js/transfun.js"}}]}
  :ring {:handler transfun.handler/war-handler
         :init transfun.handler/init
         :destroy transfun.handler/destroy}
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.3"] [ring/ring-devel "1.1.8"]]}})
