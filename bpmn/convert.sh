#!/bin/zsh

for file in *.(bpmn)(N);
camunda2flowable -s $file > ../src/main/resources/processes/$file
