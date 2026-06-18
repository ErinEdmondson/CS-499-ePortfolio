import dash
from dash import Dash, dcc, html, dash_table
import dash_leaflet as dl
import plotly.express as px
from dash.dependencies import Input, Output, State
import base64
import os
from dotenv import load_dotenv

import pandas as pd

# Load environment variables from .env file
load_dotenv()

# change animal_shelter and AnimalShelter to match your CRUD Python module file name and class name
from CRUD_Python_Module import AnimalShelter

###########################
# Data Manipulation / Model
###########################

username = os.getenv("MONGO_USERNAME")
password = os.getenv("MONGO_PASSWORD")

if not username or not password:
    raise RuntimeError("MongoDB credentials are not configured.")

# Connect to database via CRUD Module
db = AnimalShelter(username, password)

# class read method must support return of list object and accept projection json input
# sending the read method an empty document requests all documents be returned
try:
    df = pd.DataFrame.from_records(db.read({}))
except Exception as e:
    print(f"Error connecting to database or reading data: {e}")
    df = pd.DataFrame()

# MongoDB v5+ is going to return the '_id' column and that is going to have an 
# invalid object type of 'ObjectID' - which will cause the data_table to crash - so we remove
# it in the dataframe here. The df.drop command allows us to drop the column. If we do not set
# inplace=True - it will return a new dataframe that does not contain the dropped column(s)
if not df.empty and '_id' in df.columns:
    df.drop(columns=['_id'], inplace=True)

if not df.empty:
    cols = df.columns.tolist()
    if 'name' in cols and 'animal_type' in cols and 'breed' in cols:
        cols.remove('name')
        # Find index of animal_type and insert name right after it
        animal_type_idx = cols.index('animal_type')
        cols.insert(animal_type_idx + 1, 'name')
        df = df[cols]


#########################
# Dashboard Layout / View
#########################
app = Dash(__name__)

# Add in Grazioso Salvare’s logo
image_filename = 'Grazioso Salvare Logo.png' # replace with your own image
try:
    encoded_image = base64.b64encode(open(image_filename, 'rb').read())
    img_src = 'data:image/png;base64,{}'.format(encoded_image.decode())
except FileNotFoundError:
    img_src = "" # fallback if image is missing

app.layout = html.Div([
    html.Center(html.A(html.Img(src=img_src, style={"height":"90px"}), href="https://www.snhu.edu", target="_blank")),
    html.Center(html.B(html.H1('CS-340 Dashboard'))),
    html.Center(html.H4("Erin Edmondson")),
    html.Center(html.H4("Unique ID: Khaos")), #Unique ID
    html.Hr(),
    html.Div(
        dcc.RadioItems(
            id='filter-type',
            options=[
            {'label': 'Water Rescue', 'value': 'water'},
            {'label': 'Mountain or Wilderness Rescue', 'value': 'mountain'},
            {'label': 'Disaster or Individual Tracking', 'value': 'disaster'},
            {'label': 'Reset', 'value': 'reset'}
        ],
            value='reset',
            inline=True
        )
    ),
    
    html.Hr(),

        html.Div([
            # Create box for text input
        dcc.Input(id='fuzzy-search-input', type='text', placeholder='Search by breed, color, or name...', style={'width': '300px', 'marginRight': '10px'}),
        # Create button for fuzzy search
        html.Button('Fuzzy Search', id='fuzzy-search-button', n_clicks=0, style={'marginRight': '10px'}),
        # Create button for clearing search
        html.Button('Clear Search', id='clear-search-button', n_clicks=0) 
    ], style={'textAlign': 'center', 'paddingBottom': '10px'}),

    dash_table.DataTable(id='datatable-id',
                         columns=[{"name": i, "id": i, "deletable": False, "selectable": True} for i in df.columns] if not df.empty else [],
                         data=df.to_dict('records') if not df.empty else [],
                         page_action="native",
                         page_current=0,
                         page_size=10,
                         sort_action="native",
                         sort_mode="multi",
                         filter_action="native",
                         row_selectable="single",
                         selected_rows=[0],
                         style_table={'overflowX': 'auto'},
                        ),
    html.Br(),
    html.Hr(),
    html.Div(className='row',
         style={'display' : 'flex'},
             children=[
        html.Div(
            id='graph-id',
            className='col s12 m6',
            ),
        html.Div(
            id='map-id',
            className='col s12 m6',
            )
        ])
])

#############################################
# Interaction Between Components / Controller
#############################################

@app.callback(
    Output('fuzzy-search-input', 'value'),
    [Input('clear-search-button', 'n_clicks')]
)
def clear_search(n_clicks):
    if n_clicks > 0:
        return ''
    return dash.no_update
    
@app.callback(
    Output('datatable-id', 'data'),
    [Input('filter-type', 'value'),
     Input('fuzzy-search-button', 'n_clicks'),
     Input('fuzzy-search-input', 'value')]
)
def update_dashboard(filter_type, n_clicks, search_term):

    ctx = dash.callback_context
    triggered_id = ctx.triggered[0]['prop_id'].split('.')[0] if ctx.triggered else 'filter-type'
    
    if (triggered_id == 'fuzzy-search-button' or triggered_id == 'fuzzy-search-input') and search_term:
        filtered_records = db.fuzzy_search(search_term)
        filtered_df = pd.DataFrame.from_records(filtered_records)

    else:
        # Define queries for filter options
        queries = {
            "water": {
                "animal_type": "Dog",
                "breed": {"$regex": "Labrador|Chesapeake|Newfoundland", "$options": "i"},
                "sex_upon_outcome": "Intact Female",
                "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156}
            },
            "mountain": {
                "animal_type": "Dog",
                "breed": {"$regex": "German Shepherd|Alaskan Malamute|Old English Sheepdog|Siberian Husky|Rottweiler", "$options": "i"},
                "sex_upon_outcome": "Intact Male",
                "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156}
            },
            "disaster": {
                "animal_type": "Dog",
                "breed": {"$regex": "Doberman|German Shepherd|Golden Retriever|Bloodhound|Rottweiler", "$options": "i"},
                "sex_upon_outcome": "Intact Male",
                "age_upon_outcome_in_weeks": {"$gte": 20, "$lte": 300}
            },
            "reset": {} 
        }
        # Select query, read, and convert into pandas DataFrame
        query = queries.get(filter_type, {})
        filtered_records = db.read(query)
        filtered_df = pd.DataFrame.from_records(filtered_records)
    
    if not filtered_df.empty and '_id' in filtered_df.columns:
        filtered_df.drop(columns=['_id'], inplace=True)
        
    return filtered_df.to_dict('records') if not filtered_df.empty else []

@app.callback(
    Output('graph-id', "children"),
    [Input('filter-type', 'value')])
def update_graphs(filter_type):
    # Use the same exact filter queries as the table
    queries = {
        "water": {
            "animal_type": "Dog",
            "breed": {"$regex": "Labrador|Chesapeake|Newfoundland", "$options": "i"},
            "sex_upon_outcome": "Intact Female",
            "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156}
        },
        "mountain": {
            "animal_type": "Dog",
            "breed": {"$regex": "German Shepherd|Alaskan Malamute|Old English Sheepdog|Siberian Husky|Rottweiler", "$options": "i"},
            "sex_upon_outcome": "Intact Male",
            "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156}
        },
        "disaster": {
            "animal_type": "Dog",
            "breed": {"$regex": "Doberman|German Shepherd|Golden Retriever|Bloodhound|Rottweiler", "$options": "i"},
            "sex_upon_outcome": "Intact Male",
            "age_upon_outcome_in_weeks": {"$gte": 20, "$lte": 300}
        },
        "reset": {} 
    }
    
    # Query the database directly based ONLY on the radio button
    query = queries.get(filter_type, {})
    filtered_records = db.read(query)
    dff = pd.DataFrame.from_records(filtered_records)
    
    if dff.empty:
        return html.H4("No available data.")
    
    top_n = 10
    if 'breed' not in dff.columns:
        return html.H4("Breed data not available.")
        
    counts = dff['breed'].value_counts()
    
    top = counts.head(top_n)
    other_count = counts.iloc[top_n:].sum()
    
    chart_df = top.reset_index()
    chart_df.columns = ['breed', 'count']
    
    if other_count > 0:
        chart_df = pd.concat(
            [chart_df, pd.DataFrame([{'breed': 'Other', 'count': other_count}])],
            ignore_index=True
        )
    figure = px.pie(chart_df, names='breed', values='count', title='Top 10 Preferred Animals')
    return [dcc.Graph(figure=figure)]
    
@app.callback(
    Output('datatable-id', 'style_data_conditional'),
    [Input('datatable-id', 'selected_columns')]
)
def update_styles(selected_columns):
    if not selected_columns:
        return []
    return [{
        'if': { 'column_id': i },
        'background_color': '#D2F3FF'
    } for i in selected_columns]

@app.callback(
    Output('map-id', "children"),
    [Input('datatable-id', "derived_virtual_data"),
     Input('datatable-id', "derived_virtual_selected_rows")])
def update_map(viewData, index):  
    if viewData is None or len(viewData) == 0:
        return
    
    dff = pd.DataFrame.from_dict(viewData)
    
    if index is None or len(index) == 0:
        row = 0
    else:
        row = index[0]
        
    if row >= len(dff):
        row = 0
    
    if 'location_lat' not in dff.columns or 'location_long' not in dff.columns:
         return html.H4("Location data not available.")
         
    lat = dff.loc[row, 'location_lat']
    lon = dff.loc[row, 'location_long']
    
    return [
        dl.Map(style={'width': '1000px', 'height': '500px'}, center=[lat, lon], zoom=10, children=[
            dl.TileLayer(id="base-layer-id"),
            dl.Marker(position=[lat, lon], children=[
                dl.Tooltip(dff.iloc[row,4] if len(dff.columns) > 4 else "Breed"),
                dl.Popup([
                    html.H1("Animal Name"),
                    html.P(dff.iloc[row,9] if len(dff.columns) > 9 else "Unknown")
                ])
            ])
        ])
    ]

if __name__ == '__main__':
    app.run(debug=True, use_reloader=False)