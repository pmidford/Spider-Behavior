#!/bin/sh
echo "Extracting IAO"
curl -s -F file=@ontofoxIAOinput.txt -o ../ontologies/IAO_import.owl http://ontofox.hegroup.org/service.php
echo "Extracting NCBI taxonomy"
curl -s -F file=@ontofoxNCBIinput.txt -o ../ontologies/NCBI_import.owl http://ontofox.hegroup.org/service.php
echo "Extracting RO"
curl -s -F file=@ontofoxROinput.txt -o ../ontologies/RO_import.owl http://ontofox.hegroup.org/service.php
echo "Extracting NBO"
curl -s -F file=@ontofoxNBOinput.txt -o ../ontologies/NBO_import.owl http://ontofox.hegroup.org/service.php
echo "Extracting SPD"
curl -s -F file=@ontofoxSPDinput.txt -o ../ontologies/SPD_import.owl http://ontofox.hegroup.org/service.php



