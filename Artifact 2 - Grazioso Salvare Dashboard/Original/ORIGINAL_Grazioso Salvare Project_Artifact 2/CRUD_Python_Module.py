# Example Python Code to Insert a Document 

from pymongo import MongoClient 
from bson.objectid import ObjectId 

class AnimalShelter(object): 
    """ CRUD operations for Animal collection in MongoDB """ 

    def __init__(self, username, password): 
        # Initializing the MongoClient. This helps to access the MongoDB 
        # databases and collections. This is hard-wired to use the aac 
        # database, the animals collection, and the aac user. 
        # 
        # You must edit the password below for your environment. 
        # 
        # Connection Variables 
        # 
       # USER = 'aacuser' 
       # PASS = 'CherryBlossom423' 
        HOST = 'localhost' 
        PORT = 27017 
        DB = 'aac' 
        COL = 'animals' 
        # 
        # Initialize Connection 
        # 
        
        self.client = MongoClient(f"mongodb://{username}:{password}@{HOST}:{PORT}/?authSource=admin")
        self.database = self.client[DB]
        self.collection = self.database[COL]
 
            
        # self.client = MongoClient('mongodb://%s:%s@%s:%d' % (USER,PASS,HOST,PORT)) 
        #self.database = self.client['%s' % (DB)] 
        #self.collection = self.database['%s' % (COL)] 

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
            