import React from 'react';
import { InboxIcon } from 'lucide-react';
export default function EmptyState({title='Sin datos',description='',icon:Icon=InboxIcon}:{title?:string;description?:string;icon?:any}) {
  return (
    <div className="text-center py-16 px-4">
      <Icon className="w-14 h-14 text-gray-300 mx-auto mb-4"/>
      <p className="text-gray-500 font-medium text-lg">{title}</p>
      {description && <p className="text-gray-400 text-sm mt-1">{description}</p>}
    </div>
  );
}
