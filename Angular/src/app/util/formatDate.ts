
export function formatDate(dateArray: number[] | undefined): string {
    if (!dateArray) {
      return 'Unknown';
    }

    if (dateArray.length !== 3) {
      return 'Invalid date';
    }

    const [year, month, day] = dateArray;
    const date = new Date(year, month - 1, day, 15);

    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }

    return new Intl.DateTimeFormat('en-US', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);
  }
