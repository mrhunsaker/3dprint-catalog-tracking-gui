# 3D Print Catalog Tracking GUI

This site contains generated API documentation and user-facing guides for the 3D Print Catalog Tracking GUI project.

## Generating the site locally

Run:

```bash
mvn clean site
```

This will generate the site under `target/site/` and the Javadoc under `target/site/apidocs/`.

## Deploying to GitHub Pages

1. Build the site: `mvn clean site`.
2. Copy the generated `target/site` contents to the `gh-pages` branch or use the `gh-pages` action.

A simple manual push (from project root):

```bash
# generate site
mvn clean site
# create a temporary directory and switch to it
mkdir /tmp/ghpages && cd /tmp/ghpages
# clone the gh-pages branch into the tmp dir
git clone --branch gh-pages --single-branch <REPO_URL> .
# remove old files and copy newly generated site
rm -rf *
cp -r /var/home/ryhunsaker/GitHubRepos/3dprint-catalog-tracking-gui/target/site/* .
# commit and push
git add .
git commit -m "Update site"
git push origin gh-pages
```

Alternatively, use GitHub Actions or the `maven-scm-plugin` with `mvn site:deploy` configured for the repository.

