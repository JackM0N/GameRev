import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ImageCacheService {
  // Check if an image is already cached
  public isCached(url: string): boolean {
    return localStorage.getItem(url) !== null;
  }

  // Convert a blob to a data URL and cache it
  public cacheBlob(url: string, blob: Blob): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        localStorage.setItem(url, reader.result as string);
        resolve(reader.result as string);
      };
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }

  public cacheProfilePicName(url: string, name: string): void {
    localStorage.setItem(url, name);
  }

  public didPictureNameChange(url: string, currentName?: string): boolean {
    return currentName != localStorage.getItem(url);
  }

  public deleteCachedImage(url: string): void {
    localStorage.removeItem(url);
  }

  // Get an image from cache or return null if not already cached
  public getCachedImage(url: string): string | null {
    return localStorage.getItem(url);
  }
}
