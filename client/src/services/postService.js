import { API_ENDPOINTS, apiRequest } from '../config/api';
import { categoryService } from './categoryService';

class PostService {
  // Get posts with filters and pagination
  async getPosts(params = {}) {
    const queryParams = new URLSearchParams();
    
    // Add parameters if they exist
    if (params.categoryId) queryParams.append('categoryId', params.categoryId);
    if (params.categoryIds) queryParams.append('categoryIds', params.categoryIds);
    if (params.cityId) queryParams.append('cityId', params.cityId);
    if (params.regionId) queryParams.append('regionId', params.regionId);
    if (params.type) queryParams.append('type', params.type);
    if (params.condition) queryParams.append('condition', params.condition);
    if (params.minPrice) queryParams.append('minPrice', params.minPrice);
    if (params.maxPrice) queryParams.append('maxPrice', params.maxPrice);
    if (params.page !== undefined) queryParams.append('page', params.page);
    if (params.size !== undefined) queryParams.append('size', params.size);
    if (params.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params.direction) queryParams.append('direction', params.direction);
    
    const url = `${API_ENDPOINTS.posts.list}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    
    const response = await apiRequest(url, {
      method: 'GET',
    });
    
    return response;
  }
  
  // Advanced search with query and filters
  async searchPosts(params = {}) {
    const queryParams = new URLSearchParams();
    
    // Add search parameters
    if (params.q) queryParams.append('q', params.q);
    if (params.categoryId) queryParams.append('categoryId', params.categoryId);
    if (params.cityId) queryParams.append('cityId', params.cityId);
    if (params.regionId) queryParams.append('regionId', params.regionId);
    if (params.type) queryParams.append('type', params.type);
    if (params.condition) queryParams.append('condition', params.condition);
    if (params.minPrice) queryParams.append('minPrice', params.minPrice);
    if (params.maxPrice) queryParams.append('maxPrice', params.maxPrice);
    if (params.page !== undefined) queryParams.append('page', params.page);
    if (params.size !== undefined) queryParams.append('size', params.size);
    if (params.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params.direction) queryParams.append('direction', params.direction);
    if (params.sort) queryParams.append('sort', params.sort);
    
    const url = `${API_ENDPOINTS.posts.search}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    const response = await apiRequest(url, {
      method: 'GET',
    });
    
    return response;
  }
  
  // Get post by ID
  async getPostById(id) {
    const response = await apiRequest(API_ENDPOINTS.posts.getById(id), {
      method: 'GET',
    });
    return response;
  }
  
  // Create a new post
  async createPost(data) {
    // Normalize enum values and types to match backend requirements
    const normalizeType = (type) => {
      if (!type) return undefined;
      const map = {
        SALE: 'SELL',
        SELL: 'SELL',
        WANTED: 'ASK',
        ASK: 'ASK',
        EXCHANGE: 'SELL' // backend does not support EXCHANGE; fallback to SELL
      };
      return map[type] || type;
    };

    const normalizeCondition = (cond) => {
      if (!cond) return undefined;
      const map = {
        GOOD: 'USED_GOOD',
        FAIR: 'USED_FAIR',
        LIKE_NEW: 'LIKE_NEW',
        NEW: 'NEW',
        USED_GOOD: 'USED_GOOD',
        USED_FAIR: 'USED_FAIR',
        FOR_PARTS: 'FOR_PARTS'
      };
      return map[cond] || cond;
    };

    const payload = {
      title: data.title?.trim(),
      description: data.description?.trim(),
      type: normalizeType(data.type),
      condition: normalizeCondition(data.condition),
      price: data.price !== undefined && data.price !== '' ? Number(data.price) : undefined,
      priceMin: data.priceMin !== undefined && data.priceMin !== '' ? Number(data.priceMin) : undefined,
      priceMax: data.priceMax !== undefined && data.priceMax !== '' ? Number(data.priceMax) : undefined,
      categoryId: data.categoryId != null ? Number(data.categoryId) : undefined,
      cityId: data.cityId != null ? Number(data.cityId) : undefined,
      imageUrls: Array.isArray(data.imageUrls) && data.imageUrls.length > 0
        ? data.imageUrls
        : (Array.isArray(data.images) && data.images.length > 0 ? data.images : ['https://via.placeholder.com/800x600?text=Product+Image'])
    };

    const response = await apiRequest(API_ENDPOINTS.posts.create, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return response;
  }
  
  // Update a post
  async updatePost(id, data) {
    // Normalize and send only fields allowed by UpdatePostRequest
    const normalizeCondition = (cond) => {
      if (!cond) return undefined;
      const map = {
        GOOD: 'USED_GOOD',
        FAIR: 'USED_FAIR',
        LIKE_NEW: 'LIKE_NEW',
        NEW: 'NEW',
        USED_GOOD: 'USED_GOOD',
        USED_FAIR: 'USED_FAIR',
        FOR_PARTS: 'FOR_PARTS',
      };
      return map[cond] || cond;
    };

    const payload = {
      title: data.title?.trim(),
      description: data.description?.trim(),
      price: data.price !== undefined && data.price !== '' && !Number.isNaN(Number(data.price)) ? Number(data.price) : undefined,
      priceMin: data.priceMin !== undefined && data.priceMin !== '' && !Number.isNaN(Number(data.priceMin)) ? Number(data.priceMin) : undefined,
      priceMax: data.priceMax !== undefined && data.priceMax !== '' && !Number.isNaN(Number(data.priceMax)) ? Number(data.priceMax) : undefined,
      condition: normalizeCondition(data.condition),
      cityId: data.cityId !== undefined && data.cityId !== '' && !Number.isNaN(Number(data.cityId)) ? Number(data.cityId) : undefined,
      // Only send images if we actually have at least one; otherwise omit to keep existing images
      imageUrls: Array.isArray(data.imageUrls) && data.imageUrls.length > 0
        ? data.imageUrls
        : (Array.isArray(data.images) && data.images.length > 0
            ? data.images.map(img => typeof img === 'string' ? img : (img?.url ?? null)).filter(Boolean)
            : undefined),
    };

    const response = await apiRequest(API_ENDPOINTS.posts.update(id), {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
    return response;
  }
  
  // Delete a post
  async deletePost(id) {
    const response = await apiRequest(API_ENDPOINTS.posts.delete(id), {
      method: 'DELETE',
    });
    return response;
  }
  
  // Get my posts
  async getMyPosts(params = {}) {
    const queryParams = new URLSearchParams();
    
    if (params.page !== undefined) queryParams.append('page', params.page);
    if (params.size !== undefined) queryParams.append('size', params.size);
    if (params.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params.direction) queryParams.append('direction', params.direction);
    
    const url = `${API_ENDPOINTS.posts.myPosts}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    const response = await apiRequest(url, {
      method: 'GET',
    });
    
    return response;
  }
  
  // Mark post as sold
  async markAsSold(id, soldThroughPlatform) {
    const response = await apiRequest(API_ENDPOINTS.posts.markAsSold(id), {
      method: 'POST',
      body: JSON.stringify({ soldThroughPlatform }),
    });
    return response;
  }
  
  // Map category IDs to display names using cached API data
  getCategoryDisplayName(categoryId) {
    const cached = categoryService.categoriesCache;
    if (cached) {
      const cat = cached.find(c => c.id === categoryId);
      if (cat) {
        const isArabic = document.documentElement.lang === 'ar' || document.documentElement.dir === 'rtl';
        return isArabic ? cat.nameAr : cat.nameEn;
      }
    }
    return 'Uncategorized';
  }
  
  // Transform backend post to frontend product format
  transformPostToProduct(post) {
    const firstImage = post.images && post.images.length > 0 ? post.images[0] : null;
    return {
      id: post.id,
      title: post.title,
      price: post.price || post.priceMin,
      originalPrice: post.priceMax, // Use priceMax as original price for discount calculation
      image: firstImage ? firstImage.url : '/placeholder.jpg',
      thumbnailUrl: firstImage ? (firstImage.thumbnailUrl || firstImage.url) : '/placeholder.jpg',
      images: post.images || [],
      rating: 4.5, // Default rating since backend doesn't have ratings yet
      reviews: 0, // Default reviews count
      category: this.getCategoryDisplayName(post.categoryId) || post.categoryName || 'Uncategorized',
      categoryId: post.categoryId,
      description: post.description,
      condition: post.condition,
      type: post.type,
      status: post.status,
      sold: post.sold || post.status === 'SOLD',
      cityName: post.cityName,
      cityId: post.cityId,
      ownerId: post.ownerId,
      ownerUsername: post.ownerUsername,
      store: post.store,
      createdAt: post.createdAt,
      updatedAt: post.updatedAt,
      // Calculate if on sale based on price difference
      onSale: post.priceMax && post.priceMin && post.priceMax > post.priceMin,
      // Calculate discount percentage
      discount: post.priceMax && post.priceMin && post.priceMax > post.priceMin 
        ? Math.round(((post.priceMax - post.priceMin) / post.priceMax) * 100)
        : 0,
    };
  }
  
  // Transform multiple posts
  transformPosts(posts) {
    return posts.map(post => this.transformPostToProduct(post));
  }
}

const postService = new PostService();

export { postService };
export default postService;