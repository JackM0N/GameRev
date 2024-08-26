
export function formatDate(date: Date | undefined): string {
  return new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  }).format(date);
}

export function formatDateTime(date: Date | undefined): string {
  return new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric'
  }).format(date);
}

export function formatDateArray(dateArray: number[] | undefined): string {
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

  return new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  }).format(date);
}

export function formatDateTimeArray(dateTimeArray: number[] | undefined): string {
  if (!dateTimeArray) {
    return 'Unknown';
  }

  if (dateTimeArray.length < 5) {
    return 'Invalid date';
  }

  const [year, month, day, hour, minutes, seconds = 0] = dateTimeArray;
  const date = new Date(year, month - 1, day, 15);
  date.setHours(hour);
  date.setMinutes(minutes);
  date.setSeconds(seconds);

  if (isNaN(date.getTime())) {
    return 'Invalid date';
  }

  return new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric'
  }).format(date);
}
