<?php

// Use this to convert an existing concept_tree.json file
// from the original unsorted format to the new format,
// with concepts sorted by prefLabels.
// Feed in the original file as standard input;
// the updated data appears on standard output.

$text=stream_get_contents(STDIN);

$input_structure=json_decode($text);

function cmp($a, $b) {
    // Carefully imitate the ordering defined in the Toolkit method
    // provider.transform.JsonTreeTransformProvider.Concept.compareTo().
    if (!isset($a['prefLabel'])) {
        if (!isset($b['prefLabel'])) {
            return strcmp($a['iri'], $b['iri']);
        }
        return 1;
    }
    if (!isset($b['prefLabel'])) {
        return -1;
    }
    $prefLabelCmp = strcasecmp($a['prefLabel'], $b['prefLabel']);
    if ($prefLabelCmp != 0) {
        return $prefLabelCmp;
    }
    return strcmp($a['iri'], $b['iri']);
}

function make_new_structure($structure)
{
    $newstructure = array();

    foreach ($structure as $key => $value) {
        switch ($key) {
            case 'prefLabel':
            case 'definition':
            case 'notation':
                break;
        default:
            $newelement = array();
            $newelement['iri'] = $key;
            foreach ($value as $key2 => $value2) {
                switch ($key2) {
                case 'prefLabel':
                case 'definition':
                case 'notation':
                    $newelement[$key2] = $value2;
                    break;
                }
            }
            $narrower = make_new_structure($value);
            if (sizeof($narrower) > 0) {
                $newelement['narrower'] = $narrower;
            }
            $newstructure[] = $newelement;
        }
    }
    usort($newstructure, "cmp");
    return $newstructure;
}

print json_encode(make_new_structure($input_structure),
                  JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
?>
