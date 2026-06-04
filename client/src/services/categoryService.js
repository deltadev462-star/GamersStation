import { API_ENDPOINTS, apiRequest } from '../config/api';

class CategoryService {
  // Cache categories to avoid multiple API calls
  categoriesCache = null;
  
  // Get all categories
  async getCategories() {
    try {
      if (this.categoriesCache) {
        return this.categoriesCache;
      }
      
      const response = await apiRequest(API_ENDPOINTS.categories.list, {
        method: 'GET',
      });
      
      // Flatten the tree structure into a flat array
      const flatCategories = this.flattenCategoryTree(response);
      
      this.categoriesCache = flatCategories;
      return flatCategories;
    } catch (error) {
      console.error('Error fetching categories:', error);
      throw error;
    }
  }
  
  // Helper method to flatten category tree (derives parentId/level from tree depth)
  flattenCategoryTree(tree, result = [], parentId = null, level = 1) {
    for (const node of tree) {
      result.push({
        id: node.id,
        nameEn: node.nameEn,
        nameAr: node.nameAr,
        slug: node.slug,
        parentId: parentId,
        level: level,
        sortOrder: node.sortOrder,
        isActive: node.isActive,
      });
      
      if (node.children && node.children.length > 0) {
        this.flattenCategoryTree(node.children, result, node.id, level + 1);
      }
    }
    return result;
  }
  
  // Get category by ID
  async getCategoryById(id) {
    try {
      const response = await apiRequest(API_ENDPOINTS.categories.getById(id), {
        method: 'GET',
      });
      return response;
    } catch (error) {
      console.error('Error fetching category:', error);
      throw error;
    }
  }
  
  // Get top-level categories (level 1)
  async getMainCategories() {
    const categories = await this.getCategories();
    return categories.filter(cat => cat.level === 1);
  }
  
  // Get subcategories by parent ID
  async getSubcategories(parentId) {
    const categories = await this.getCategories();
    return categories.filter(cat => cat.parentId === parentId);
  }
  
  // Find category by slug
  async getCategoryBySlug(slug) {
    const categories = await this.getCategories();
    return categories.find(cat => cat.slug === slug);
  }
  
  // Get gaming platform categories
  async getGamingPlatforms() {
    const categories = await this.getCategories();
    
    // Find the gaming consoles parent category
    const gamingConsolesCategory = categories.find(cat => cat.slug === 'gaming-consoles');
    if (!gamingConsolesCategory) return [];
    
    // Get all subcategories of gaming consoles
    const platformCategories = categories.filter(cat => cat.parentId === gamingConsolesCategory.id);
    
    // Add PC gaming category
    const pcGamingCategory = categories.find(cat => cat.slug === 'pc-gaming');
    if (pcGamingCategory) {
      platformCategories.push(pcGamingCategory);
    }
    
    // Add gaming accessories category
    const accessoriesCategory = categories.find(cat => cat.slug === 'gaming-accessories');
    if (accessoriesCategory) {
      platformCategories.push(accessoriesCategory);
    }
    
    return platformCategories;
  }
  
  // Clear cache
  clearCache() {
    this.categoriesCache = null;
  }
}

const categoryService = new CategoryService();

export { categoryService };
export default categoryService;