export SESAME_REPOS="${HOME}/.aduna/repositories"
rm ./src/main/resources/arachb.owl
cp ../arachtools/owlbuilder/test.owl ./src/main/resources/arachb.owl
rm -rf "$SESAME_REPOS"/test1