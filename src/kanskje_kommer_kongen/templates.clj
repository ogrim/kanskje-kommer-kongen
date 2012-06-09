(ns kanskje-kommer-kongen.templates
  (:use [net.cgrand.enlive-html]))

(defmacro maybe-content
  ([expr] `(if-let [x# ~expr] (content x#) identity))
  ([expr & exprs] `(maybe-content (or ~expr ~@exprs))))

(defmacro maybe-substitute
  ([expr] `(if-let [x# ~expr] (substitute x#) identity))
  ([expr & exprs] `(maybe-substitute (or ~expr ~@exprs))))

(defn str->html [string]
  (-> string
      java.io.StringReader.
      html-resource
      (select [:body])
      first
      :content))

(deftemplate base "base.html" [{:keys [h1 main meta]}]
  [:meta] (maybe-substitute meta)
  [:ul#top-menu :li] (content (top-menu))
  [:h1]  (maybe-content h1)
  [:div#content] (maybe-content main))

(defsnippet menu "snippets.html" [:ul#menu]
  []
  [:li] (clone-for [[name href] start-menu-data]
                   [:a] (set-attr :href href)
                   [:a] (content name)))

(defn start-page [] (base {:main (menu)}))
