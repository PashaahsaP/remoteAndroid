package com.example.wmsRemote.Adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter

data class CatalogItem(val id: Int, val name: String){
    override fun toString(): String {
        return name
    }
}
class CatalogAdapter(context: Context, private var items: MutableList<CatalogItem>) :
    ArrayAdapter<CatalogItem>(context, android.R.layout.simple_dropdown_item_1line, items) {

    private var filteredItems: MutableList<CatalogItem> = items.toMutableList()

    override fun getCount(): Int {
        return filteredItems.size
    }

    override fun getItem(position: Int): CatalogItem {
        return filteredItems[position]
    }

    override fun getItemId(position: Int): Long {
        return filteredItems[position].id.toLong() // Return the ID as the item ID
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        // Customize the view if needed (e.g., set text color)
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val query = constraint.toString().lowercase().trim()

                // Filter the items based on the query
                results.values = if (query.isEmpty()) {
                    items // Return original list if query is empty
                } else {
                    items.filter { it.name.lowercase().contains(query) } // Filter based on name
                }

                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems.clear()
                filteredItems.addAll(results?.values as List<CatalogItem>)
                notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
    }
}