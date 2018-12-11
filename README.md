# Littlesearch

Littlesearch est avant tout un **moteur de recherche**.  

Si nous l'avons nommé Littlesearch, c'est tout d'abord par référence à Elasticsearch : non seulement, Littlesearch, utilise-t-il, lui aussi, [Lucene](http://lucene.apache.org/), mais aussi se base-t-il, lui aussi, sur des informations hiérarchisées selon un ou deux formats JSON.  
Toutefois, la comparaison s'arrête là, Littlesearch étant un tout *petit* projet, "little search" pouvant être traduit, littéralement, en effet, par "petite recherche", d'autant que le nombre des documents dans lesquels Littlesearch effectue les recherches est petit.  

Dès lors, quel intérêt pouvait-il y avoir, au-delà de l'intérêt de savoir comment fonctionne un moteur de recherche, à développer un moteur de recherche aussi modeste que Littlesearch mais basé sur les mêmes outils que certains de ceux mis en œuvre au cœur d'Elasticsearch ?  

Littlesearch a été conçu pour traiter du texte en français ; or, en développant le projet, nous avons découvert une faiblesse de certains outils proposés par Lucene : par exemple, le [FrenchStemmer](https://lucene.apache.org/core/7_5_0/analyzers-common/org/tartarus/snowball/ext/FrenchStemmer.html) ne permet pas toujours d'obtenir une valeur satisfaisante. (Nous avons écrit une classe pour voir un peu ce que l'outil en question nous retourne : la classe "**FrenchStemmerTest**".)  

...  

## Compilation

[Maven](https://maven.apache.org/) : `mvn clean package`  

## Exécution

Le script shell "**searchFor**" : `./searchFor <words>...`  

Par exemple : `./searchFor Riemann`  
ou : `./searchFor "Riemann"`  
et : `./searchFor Riemann travail`  
ou : `./searchFor "Riemann travail"`  
ou encore : `./searchFor "Riemann" "travail"`  

## Liens utiles

[Javadoc]()  

## Auteurs

L'équipe **fromAtoZ** :
* Andrei ZABOLOTNÎI
* Cyril MARILIER
