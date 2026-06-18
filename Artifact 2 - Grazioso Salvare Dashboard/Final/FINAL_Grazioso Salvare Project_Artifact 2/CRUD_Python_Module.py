# Example Python Code to Insert a Document 

from pymongo import MongoClient 
from bson.objectid import ObjectId 

class AnimalShelter(object): 
    """ CRUD operations for Animal collection in MongoDB """ 

    def __init__(self, username, password): 
        # Initializing the MongoClient. This helps to access the MongoDB 
        # databases and collections. This is hard-wired to use the aac 
        # 
        # Connection Variables 
        # 
        DB = 'aac' 
        COL = 'animals' 
        # 
        # Initialize Connection 
        # 
        
        uri = f"mongodb+srv://{username}:{password}@cluster0.xr7r3o1.mongodb.net/"
        self.client = MongoClient(uri)
        self.database = self.client[DB]
        self.collection = self.database[COL]

    # Create a method to return the next available record number for use in the create method
            
    # Complete this create method to implement the C in CRUD. 
    def create(self, data):
        if data is not None:
            try: 
                self.collection.insert_one(data)  # data should be dictionary
                return True
            except Exception:
                return False
        else: 
            return False

    # Create method to implement the R in CRUD.
    def read(self, data):
        try: 
            cursor = self.collection.find(data)
            return list(cursor)
        except Exception:
            return []
        
    # Create method to implement the U in CRUD 
    def update(self, query, new_value, many=True):
        if query is not None and new_value is not None: # if args are provided
            try:
                if many: # update many docs if many is True
                    result = self.collection.update_many(query, new_value)
                else: # else only update one document
                    result = self.collection.update_one(query, new_value) 
                # return docs modified/updated
                return result.modified_count
            except Exception:
                return 0
        else: 
            return 0
        
    # Create method to implement D in CRUD 
    def delete(self, query, many=True): 
        if query is not None: # if argument is provided
            try:
                if many: # delete many docs if many is true
                    result = self.collection.delete_many(query)
                else: # else delete a single doc
                    result = self.collection.delete_one(query)
                # return number of docs deleted
                return result.deleted_count
            except Exception:
                return 0
        else: 
            return 0

       # Create search conditions for MongoDB Atlas to match     
    def fuzzy_search(self, search_term):
        should_clauses = [
            {
                # Search for exact terms in fields for breed, color, and name
                "text": {
                    "query": search_term,
                    "path": ["breed", "color", "name"],
                    "score": {"boost": {"value": 5}} # Give these matches a higher score so they show before other potential results
                }
            },
            {
                # Search so that partial input will pull results that match
                # An example is typing in "lab" allows matching for "labrador"
                "wildcard": {
                    "query": f"{search_term}*", 
                    "path": ["breed", "color", "name"],
                    "allowAnalyzedField": True # Allow wildcard searching on analyzed text fields
                }
            }
        ]
        # Use fuzzy match when search is longer than 3 characters. This is to avoid as many inaccurate results for short user input
        if len(search_term) > 3:
            # 1 typo allowed for short words, 2 for longer words
            edits = 1 if len(search_term) <= 6 else 2
            # Add fuzzy text search to allow minor typos
            should_clauses.append({
                "text": {
                    "query": search_term,
                    "path": ["breed", "color", "name"],
                    "fuzzy": {
                        "maxEdits": edits 
                    }
                }
            })
            # Define MongoDB aggregation pipeline
        pipeline = [
            {
                # Use search index named default
                "$search": {
                    "index": "default",
                    # Use search rules and return documents that match at least one 
                    "compound": {
                        "should": should_clauses
                    }
                }
            },
         
            {
                # Add field named searchScore so MongoDB Atlas can score based on how relevant a match is 
                "$addFields": {
                    "searchScore": { "$meta": "searchScore" }
                }
            },
            {
                # Sort by relevance with higher score appearing first
                "$sort": {
                    "searchScore": -1
                }
            }
        ]
        
        try:
            # Run pipeline on collection
            cursor = self.collection.aggregate(pipeline)
            # Convert to a list and return
            return list(cursor)
        except Exception as e:
            # Print error message if search fails
            print(f"Fuzzy search error: {e}")
            # Return empty
            return []