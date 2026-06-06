import React from 'react';
import { UtensilsCrossed, MapPin, Phone, Mail, Clock, Facebook, Instagram } from 'lucide-react';

export default function Footer() {
  return (
    <footer id="contact" className="bg-gray-900 dark:bg-black text-gray-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-14">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10">
          <div>
            <div className="flex items-center gap-2.5 mb-4"><div className="w-9 h-9 bg-gradient-to-br from-gold-400 to-gold-500 rounded-xl flex items-center justify-center"><UtensilsCrossed className="w-5 h-5 text-white"/></div><div><p className="text-white font-display font-bold">Cafetería</p><p className="text-gold-400 text-xs font-semibold">UIDE</p></div></div>
            <p className="text-sm leading-relaxed text-gray-400">Sabores ecuatorianos auténticos en el corazón de la Universidad Internacional del Ecuador.</p>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4 flex items-center gap-2"><Clock className="w-4 h-4 text-gold-400"/>Horarios</h4>
            <ul className="space-y-2 text-sm"><li className="flex justify-between"><span>Lunes - Viernes</span><span className="text-white">7:00 - 20:00</span></li><li className="flex justify-between"><span>Sábados</span><span className="text-white">8:00 - 15:00</span></li><li className="flex justify-between"><span>Domingos</span><span className="text-gray-500">Cerrado</span></li></ul>
          </div>
          <div id="about">
            <h4 className="text-white font-semibold mb-4">Quiénes Somos</h4>
            <ul className="space-y-2 text-sm"><li><a href="#" className="hover:text-gold-400 transition-colors">Nuestra Historia</a></li><li><a href="#" className="hover:text-gold-400 transition-colors">Equipo</a></li><li><a href="#" className="hover:text-gold-400 transition-colors">Política de Devoluciones</a></li><li><a href="#" className="hover:text-gold-400 transition-colors">Términos y Condiciones</a></li><li><a href="#" className="hover:text-gold-400 transition-colors">Trabaja con Nosotros</a></li></ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Contacto</h4>
            <ul className="space-y-3 text-sm"><li className="flex items-start gap-2"><MapPin className="w-4 h-4 text-gold-400 mt-0.5 flex-shrink-0"/><span>Av. Simón Bolívar y Jorge Fernández, Quito</span></li><li className="flex items-center gap-2"><Phone className="w-4 h-4 text-gold-400"/><span>(02) 398-9400</span></li><li className="flex items-center gap-2"><Mail className="w-4 h-4 text-gold-400"/><span>cafeteria@uide.edu.ec</span></li></ul>
            <div className="flex gap-3 mt-4"><a href="#" className="w-9 h-9 bg-gray-800 hover:bg-gold-400 hover:text-gray-900 rounded-lg flex items-center justify-center transition-all"><Facebook className="w-4 h-4"/></a><a href="#" className="w-9 h-9 bg-gray-800 hover:bg-gold-400 hover:text-gray-900 rounded-lg flex items-center justify-center transition-all"><Instagram className="w-4 h-4"/></a></div>
          </div>
        </div>
      </div>
      <div className="border-t border-gray-800 py-5 text-center text-xs text-gray-500">© {new Date().getFullYear()} Cafetería UIDE — Universidad Internacional del Ecuador. Todos los derechos reservados.</div>
    </footer>
  );
}
