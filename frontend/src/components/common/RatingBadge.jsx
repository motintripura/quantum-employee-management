import { RATING_LABELS } from '../../utils/constants';

export default function RatingBadge({ rating }) {
  let cls = 'bg-secondary';
  if (rating >= 4) cls = 'bg-success';
  else if (rating === 3) cls = 'bg-warning text-dark';
  else if (rating === 2) cls = 'bg-orange text-white';
  else if (rating === 1) cls = 'bg-danger';

  return (
    <span className={`badge rounded-pill ${cls}`} title={RATING_LABELS[rating]}>
      {rating} {rating === 1 ? 'Star' : 'Stars'}
    </span>
  );
}