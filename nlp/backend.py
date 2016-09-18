from flask import Flask, jsonify
from flask import request
from word2vec import get_similar_items, get_similar_anything
app = Flask(__name__)

@app.route("/get_similar_items")
def similar():
    item_name = request.args.get('item')

    try:
        results = get_similar_items(item_name)
        return jsonify(**results)
    except Exception as e:
        print("Unexpected Error")
        return "Unexpected Error\n" + str(e)

@app.route("/similar_fun")
def similar_fun():
    item_name = request.args.get('item')

    try:
        results = get_similar_anything(item_name)
        return jsonify(**results)
    except Exception as e:
        print("Unexpected Error")
        return "Unexpected Error\n" + str(e)


if __name__ == "__main__":
    app.run()
