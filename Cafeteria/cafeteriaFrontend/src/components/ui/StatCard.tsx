import React from 'react';
export default function StatCard({label,value,icon:Icon,color='text-brand-500',bgColor='bg-brand-50',onClick}:{label:string;value:string|number;icon:any;color?:string;bgColor?:string;onClick?:()=>void}) {
  return (
    <div onClick={onClick} className={`card p-5 hover:shadow-md transition-all ${onClick?'cursor-pointer hover:-translate-y-0.5':''}`}>
      <div className="flex items-start justify-between">
        <div><p className="text-sm text-gray-500 dark:text-gray-400 mb-1">{label}</p><p className="text-2xl font-bold text-gray-900 dark:text-white">{value}</p></div>
        <div className={`w-11 h-11 ${bgColor} dark:bg-opacity-20 rounded-xl flex items-center justify-center`}><Icon className={`w-5 h-5 ${color}`}/></div>
      </div>
    </div>
  );
}
