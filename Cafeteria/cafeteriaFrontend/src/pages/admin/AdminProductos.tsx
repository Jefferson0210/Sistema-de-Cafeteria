import React, { useState, useEffect } from 'react';
import { Plus, Search, Edit2, Trash2, Package, Upload, X } from 'lucide-react';
import { productService } from '../../services/productService';
import { categoryService } from '../../services/categoryService';
import { Producto, Category } from '../../types';
import { money } from '../../utils/format';
import Modal from '../../components/ui/Modal';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import toast from 'react-hot-toast';

export default function AdminProductos() {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [categorias, setCategorias] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [catFilter, setCatFilter] = useState<number | null>(null);
  const [modal, setModal] = useState(false);
  const [editing, setEditing] = useState<Producto | null>(null);
  const [saving, setSaving] = useState(false);
  const [f, setF] = useState({ nombre: '', descripcion: '', precio: '', stock: '', disponible: true, imagenUrl: '', categoryId: 0 });
  const [imagenFile, setImagenFile] = useState<File | null>(null);
  const [imagenPreview, setImagenPreview] = useState<string | null>(null);

  const load = () => { productService.listar().then(r => setProductos(r.data.data || [])).catch(() => {}).finally(() => setLoading(false)); };
  useEffect(() => { load(); categoryService.listar().then(r => setCategorias(r.data.data || [])).catch(() => {}); }, []);

  const filtered = productos.filter(p => {
    if (search && !p.nombre.toLowerCase().includes(search.toLowerCase())) return false;
    if (catFilter && p.category?.categoryId !== catFilter) return false;
    return true;
  });

  const openNew = () => {
    setEditing(null);
    setF({ nombre: '', descripcion: '', precio: '', stock: '', disponible: true, imagenUrl: '', categoryId: categorias[0]?.categoryId || 0 });
    setImagenFile(null);
    setImagenPreview(null);
    setModal(true);
  };

  const openEdit = (p: Producto) => {
    setEditing(p);
    setF({ nombre: p.nombre, descripcion: p.descripcion, precio: String (p.precio), stock: String(p.stock), disponible: p.disponible, imagenUrl: p.imagenUrl || '', categoryId: p.category?.categoryId || 0 });
    setImagenFile(null);
    setImagenPreview(p.imagenUrl || null);
    setModal(true);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setImagenFile(file);
      const reader = new FileReader();
      reader.onloadend = () => setImagenPreview(reader.result as string);
      reader.readAsDataURL(file);
    }
  };

  const clearImage = () => {
    setImagenFile(null);
    setImagenPreview(editing?.imagenUrl || null);
  };

  const save = async () => {
    setSaving(true);
    try {
      let producto;
      if (editing) {
        producto = (await productService.actualizar(editing.id, f)).data.data;
      } else {
        producto = (await productService.crear(f)).data.data;
      }

      if (imagenFile && producto?.id) {
        await productService.subirImagen(producto.id, imagenFile);
      }

      toast.success(editing ? 'Producto actualizado' : 'Producto creado');
      setModal(false);
      setImagenFile(null);
      setImagenPreview(null);
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Error');
    }
    setSaving(false);
  };

  const remove = async (id: number) => {
    if (!confirm('¿Eliminar producto?')) return;
    try { await productService.eliminar(id); toast.success('Eliminado'); load(); } catch { toast.error('Error'); }
  };

  const getImageSrc = (p: Producto) => {
    if (p.imagenUrl && p.imagenUrl.startsWith('/uploads')) return p.imagenUrl;
    if (p.imagenUrl && p.imagenUrl.startsWith('http')) return p.imagenUrl;
    return null;
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Productos</h1><p className="text-gray-500 text-sm mt-1">{productos.length} productos registrados</p></div>
        <button onClick={openNew} className="btn-gold"><Plus className="w-4 h-4" />Nuevo Producto</button>
      </div>

      <div className="card p-4 flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[200px]"><Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" /><input className="input pl-9" placeholder="Buscar productos..." value={search} onChange={e => setSearch(e.target.value)} /></div>
        <select className="input !w-auto" value={catFilter || ''} onChange={e => setCatFilter(e.target.value ? Number(e.target.value) : null)}>
          <option value="">Todas las categorías</option>
          {categorias.map(c => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
        </select>
      </div>

      {filtered.length === 0 ? <EmptyState title="Sin productos" icon={Package} /> : (
        <div className="table-container"><table className="w-full">
          <thead><tr><th className="th">Producto</th><th className="th">Imagen</th><th className="th">Categoría</th><th className="th">Precio</th><th className="th">Stock</th><th className="th">Estado</th><th className="th">Acciones</th></tr></thead>
          <tbody>{filtered.map(p => (
            <tr key={p.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50">
              <td className="td"><div><p className="font-medium text-gray-900 dark:text-white">{p.nombre}</p><p className="text-xs text-gray-400 mt-0.5 line-clamp-1">{p.descripcion}</p></div></td>
              <td className="td">
                {getImageSrc(p) ? (
                  <img src={getImageSrc(p)!} alt={p.nombre} className="w-12 h-12 rounded-lg object-cover border border-gray-200 dark:border-gray-700" />
                ) : (
                  <div className="w-12 h-12 bg-gray-100 dark:bg-gray-800 rounded-lg flex items-center justify-center text-xl">🍽️</div>
                )}
              </td>
              <td className="td text-sm">{p.category?.name || '—'}</td>
              <td className="td font-semibold text-gray-900 dark:text-white">{money(p.precio)}</td>
              <td className="td"><span className={`badge ${p.stock <= 5 ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>{p.stock}</span></td>
              <td className="td"><span className={`badge ${p.disponible ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>{p.disponible ? 'Activo' : 'Inactivo'}</span></td>
              <td className="td"><div className="flex gap-1">
                <button onClick={() => openEdit(p)} className="btn-ghost btn-xs"><Edit2 className="w-3.5 h-3.5" /></button>
                <button onClick={() => remove(p.id)} className="btn-ghost btn-xs text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20"><Trash2 className="w-3.5 h-3.5" /></button>
              </div></td>
            </tr>
          ))}</tbody>
        </table></div>
      )}

      <Modal open={modal} onClose={() => setModal(false)} title={editing ? 'Editar Producto' : 'Nuevo Producto'} size="max-w-xl">
        <div className="space-y-4">
          <div><label className="label">Nombre</label><input className="input" value={f.nombre} onChange={e => setF(p => ({ ...p, nombre: e.target.value }))} /></div>
<div><label className="label">Descripción</label><textarea className="input resize-none h-20" value={f.descripcion} onChange={e => setF(p => ({ ...p, descripcion: e.target.value }))} /></div>
<div className="grid grid-cols-3 gap-3">
  <div><label className="label">Precio</label><input className="input" type="number" step="0.01" min="0" value={f.precio} onChange={e => setF(p => ({ ...p, precio: e.target.value }))} onFocus={e => { if (e.target.value === '0') setF(p => ({ ...p, precio: '' })) }} /></div>
  <div><label className="label">Stock</label><input className="input" type="number" min="0" value={f.stock} onChange={e => setF(p => ({ ...p, stock: e.target.value }))} onFocus={e => { if (e.target.value === '0') setF(p => ({ ...p, stock: '' })) }} /></div>
  <div><label className="label">Categoría</label><select className="input" value={f.categoryId} onChange={e => setF(p => ({ ...p, categoryId: +e.target.value }))}>
    <option value={0}>Seleccionar</option>{categorias.map(c => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
  </select></div>
</div>

          {/* Imagen upload */}
          <div>
            <label className="label"><Upload className="w-3.5 h-3.5 inline mr-1" />Imagen del producto</label>
            {imagenPreview && (
              <div className="relative inline-block mb-3">
                <img src={imagenPreview} alt="Preview" className="w-32 h-32 rounded-xl object-cover border-2 border-gray-200 dark:border-gray-700" />
                {imagenFile && (
                  <button onClick={clearImage} className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 shadow-lg">
                    <X className="w-3 h-3" />
                  </button>
                )}
              </div>
            )}
            <input
              type="file"
              accept="image/*"
              className="input file:mr-3 file:py-1.5 file:px-3 file:rounded-lg file:border-0 file:bg-gold-400 file:text-gray-900 file:font-semibold file:text-xs file:cursor-pointer hover:file:bg-gold-500"
              onChange={handleFileChange}
            />
            {imagenFile && <p className="text-xs text-gray-500 dark:text-gray-400 mt-1.5 flex items-center gap-1">📎 {imagenFile.name} ({(imagenFile.size / 1024).toFixed(0)} KB)</p>}
          </div>

          <label className="flex items-center gap-2 cursor-pointer"><input type="checkbox" className="w-4 h-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500" checked={f.disponible} onChange={e => setF(p => ({ ...p, disponible: e.target.checked }))} /><span className="text-sm text-gray-700 dark:text-gray-300">Disponible</span></label>
          <div className="flex gap-3 pt-2">
            <button onClick={() => setModal(false)} className="btn-outline flex-1">Cancelar</button>
            <button onClick={save} disabled={saving} className="btn-gold flex-1">{saving ? 'Guardando...' : 'Guardar'}</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}