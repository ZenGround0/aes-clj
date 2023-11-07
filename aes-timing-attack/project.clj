(defproject aes-timing-attack "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [buddy/buddy-core "1.11.423"]
                 [ring/ring-core "1.9.2"]
                 [ring/ring-jetty-adapter "1.9.2"]
                 [compojure "1.6.2"]]
  
  :repl-options {:init-ns aes-timing-attack.core}
  :jvm-opts ["-Xmx4g"]  
  :main aes-timing-attack.core
  )
