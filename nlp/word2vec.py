import gensim
import csv
import re
from pprint import pprint

# transformed = []
# with open('waste.csv', newline='') as mycsvfile:
#     thedata = csv.reader(mycsvfile, delimiter=',', quotechar='"')
#     for row in thedata:
#         transformed.append(list(map(lambda x: re.sub('[^0-9a-zA-Z]+', ' ', x.lower()), row)))
#
# filtered = []
# for row in transformed:
#     new_row = list(map(lambda x : x.split(), row))
#     new_row = list(map(lambda y : list(filter(lambda x : x in model.vocab, y)), new_row))
#     filtered.append(list(map(lambda y : ' '.join(y), new_row)))
#
# with open('transformed.csv', 'w', newline='') as csvfile:
#     spamwriter = csv.writer(csvfile, delimiter=',',
#                             quotechar='"', quoting=csv.QUOTE_MINIMAL)
#     spamwriter.writerows(filtered)
#
# print(filtered[0:5])

# Load Google's pre-trained Word2Vec model.
model = gensim.models.Doc2Vec.load('wiki6B50D.model')

def get_wastes():
    waste_entries = []
    with open('transformed.csv', newline='') as mycsvfile:
        thedata = csv.reader(mycsvfile, delimiter=',', quotechar='"')
        for row in thedata:
            waste_entries.append(row)
    waste_entries.pop(0)

    features_entries = {}
    for row in waste_entries:
        new_string = row[1] +' ' +row[2]
        #new_string = list(set([x for x in new_string.split()]))
        features_entries[new_string] = row[4]
    return features_entries

def get_similar_items(inputs):
    features_entries = get_wastes()
    inputs = list(filter(lambda x : x in model.vocab, re.sub('[^0-9a-zA-Z]+', ' ', inputs.lower()).split()))

    results = {'similar_item': 'None', 'instructions': 'None', 'score': 0}

    for row in features_entries.items():
        features = row[0].split()
        instructions = row[1]
        if len(features) < 1 or len(inputs) < 1:
            continue
        current_score = model.n_similarity(inputs, features)

        try:
            if current_score > results['score']:
                results['similar_item'] = list(set(features))
                results['instructions'] = instructions
                results['score'] = current_score
        except:
            pass

    return results

def get_similar_anything(inputs):
    inputs = list(filter(lambda x : x in model.vocab, re.sub('[^0-9a-zA-Z]+', ' ', inputs.lower()).split()))
    return {'similar_words' : model.most_similar(inputs)}

def test():
    pprint(get_similar_items('yogurt computer'))

if __name__ == '__main__':
    test()
