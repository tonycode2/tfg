import React, { useEffect, useState } from 'react';
import { authService } from '../../services/authService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';

type Role = 'ADMIN' | 'HR' | 'JEFE' | 'EMPLEADO';

interface HorasExtraDTO {
  id?: number;
  fechaSolicitud: string;
  cantidadDeHoras: number;
  motivo: string;
  aprobado?: boolean;
  procesado?: boolean;
  estadoSolicitud?: string;
  tipoTarifa: string;
  idEmpleado?: number;
  nombreEmpleado?: string;
  primerApellidoEmpleado?: string;
}

const API_BASE = 'http://localhost:8080';

export default function HorasExtraPendientesView() {
  function formatoEstado(s?: string) {
    switch (s) {
      case 'PENDIENTE':
        return 'Pendiente';
      case 'PENDIENTE_RH':
        return 'Pendiente RH';
      case 'APROBADA_POR_JEFE':
        return 'Aprobada por jefe';
      case 'RECHAZADA_POR_JEFE':
        return 'Rechazada por jefe';
      case 'RECHAZADA_POR_RH':
        return 'Rechazada por RH';
      case 'APROBADA':
        return 'Aprobada';
      case 'CANCELADA':
        return 'Cancelada';
      default:
        return s || '';
    }
  }

  const [lista, setLista] = useState<HorasExtraDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const userInfo = authService.getUserInfo();
  const role = (userInfo.role || 'EMPLEADO') as Role;
  const token = localStorage.getItem('token') || '';

  useEffect(() => {
    fetchLista();
  }, []);

  async function fetchLista() {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE}/api/horas-extra`, {
        headers: { Authorization: token ? `Bearer ${token}` : '' },
      });
      if (!res.ok) throw new Error('Error al obtener solicitudes');
      const data = await res.json();
      if (!Array.isArray(data)) {
        setLista([]);
        return;
      }

      let filtered: HorasExtraDTO[] = [];
      if (role === 'JEFE') {
        filtered = data.filter((d: any) => d.estadoSolicitud === 'PENDIENTE');
      } else if (role === 'HR' || role === 'ADMIN') {
        filtered = data.filter((d: any) => d.estadoSolicitud === 'PENDIENTE_RH' || d.estadoSolicitud === 'APROBADA_POR_JEFE');
      }
      setLista(filtered || []);
    } catch (e) {
      console.error(e);
      toast.error('No se pudo cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  }

  async function handleAprobar(id: number) {
    try {
      let path = '';
      if (role === 'JEFE') path = `/api/horas-extra/${id}/aprobar-jefe`;
      else if (role === 'HR' || role === 'ADMIN') path = `/api/horas-extra/${id}/aprobar-rh`;
      else return toast.error('Sin permisos');

      const res = await fetch(`${API_BASE}${path}`, {
        method: 'PUT',
        headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      });
      if (!res.ok) throw new Error('Error al aprobar');
      toast.success('Aprobado');
      fetchLista();
    } catch (e) {
      console.error(e);
      toast.error('No se pudo aprobar');
    }
  }

  async function handleRechazar(id: number) {
    try {
      let path = '';
      if (role === 'JEFE') path = `/api/horas-extra/${id}/rechazar-jefe`;
      else if (role === 'HR' || role === 'ADMIN') path = `/api/horas-extra/${id}/rechazar-rh`;
      else return toast.error('Sin permisos');

      const res = await fetch(`${API_BASE}${path}`, {
        method: 'PUT',
        headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      });
      if (!res.ok) throw new Error('Error al rechazar');
      toast.success('Rechazado');
      fetchLista();
    } catch (e) {
      console.error(e);
      toast.error('No se pudo rechazar');
    }
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Horas Extra Pendientes</CardTitle>
          <CardDescription>Revisa y aprueba solicitudes pendientes de tus empleados</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center p-8">Cargando...</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm table-fixed">
                <thead>
                  <tr className="text-left text-muted-foreground">
                    <th className="py-2 px-3">Empleado</th>
                    <th className="py-2 px-3 w-32">Fecha</th>
                    <th className="py-2 px-3">Motivo</th>
                    <th className="py-2 px-3 w-24">Horas</th>
                    <th className="py-2 px-3 w-48">Estado</th>
                    <th className="py-2 px-3 w-48">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {lista.map((item) => (
                    <tr key={item.id} className="border-t align-top">
                        <td className="py-2 px-3">{item.nombreEmpleado} {item.primerApellidoEmpleado}</td>
                        <td className="py-2 px-3">{item.fechaSolicitud}</td>
                        <td className="py-2 px-3 max-w-md break-words">{item.motivo || ''}</td>
                        <td className="py-2 px-3">{item.cantidadDeHoras}</td>
                      <td className="py-2 px-3">{formatoEstado(item.estadoSolicitud)}</td>
                      <td className="py-2 px-3">
                        <div className="flex gap-2">
                          <Button variant="ghost" size="sm" onClick={() => handleAprobar(item.id!)}>Aprobar</Button>
                          <Button variant="destructive" size="sm" onClick={() => handleRechazar(item.id!)}>Rechazar</Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
