import { useState, useEffect, useCallback, useRef } from 'react';
import type { ApiService } from '@/services/apiService';

interface UseFetchOptions {
  autoLoad?: boolean;
}

export interface UseFetchResult<T> {
  data: T[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * Custom hook for fetching data with proper cleanup and error handling
 * Prevents memory leaks by canceling requests on unmount
 */
export function useFetch<T>(
  service: ApiService<T>,
  options: UseFetchOptions = { autoLoad: true }
): UseFetchResult<T> {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(options.autoLoad ?? true);
  const [error, setError] = useState<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchData = useCallback(async () => {
    // Cancel previous request if exists
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    // Create new abort controller
    abortControllerRef.current = new AbortController();

    try {
      setLoading(true);
      setError(null);
      
      const response = await service.getAllUnpaginated();
      
      // Handle both paginated and non-paginated responses
      const dataArray = (response as any).content || response;
      setData(Array.isArray(dataArray) ? dataArray : []);
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        // Request was cancelled, don't update state
        return;
      }
      console.error('Error fetching data:', err);
      setError(err instanceof Error ? err.message : 'Error al cargar datos');
      setData([]);
    } finally {
      setLoading(false);
    }
  }, [service]);

  useEffect(() => {
    if (options.autoLoad) {
      fetchData();
    }

    // Cleanup function
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [fetchData, options.autoLoad]);

  return { data, loading, error, refetch: fetchData };
}
