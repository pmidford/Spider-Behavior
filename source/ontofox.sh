#!/bin/sh
echo "Extracting IAO"
curl -s -F file=@ontofoxIAOinput.txt -o ../ontologies/IAO_input_test.owl http://ontofox.hegroup.org/service.php
echo "Extracting NCBI taxonomy"
curl -s -F file=@ontofoxNCBIinput.txt -o ../ontologies/NCBI_input_test.owl http://ontofox.hegroup.org/service.php
echo "Extracting NBO"
curl -s -F file=@ontofoxNBOinput.txt -o ../ontologies/NBO_input_test.owl http://ontofox.hegroup.org/service.php
echo "Extracting SPD"
curl -s -F file=@ontofoxSPDinput.txt -o ../ontologies/SPD_input_test.owl http://ontofox.hegroup.org/service.php



