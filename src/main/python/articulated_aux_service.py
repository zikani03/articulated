'''Auxilliary services for articulated
'''
from flask import Flask, request, g, jsonify
from text_scrubber.geo import (find_city_in_string, find_country_in_string,
                               find_region_in_string)

app = Flask(__name__)

@app.post('/extract/locations')
def extract_locations():
    body = request.get_data(as_text=True)
    countries = find_country_in_string(body)

    country_name_set = { c.location.canonical_name for c in countries }

    return jsonify(find_city_in_string(body, country_name_set | {"Malawi"}))


if __name__ == '__main__':
    app.run(port=8001)