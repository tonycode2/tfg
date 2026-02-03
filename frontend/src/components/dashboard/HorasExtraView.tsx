import React, { useEffect, useState } from 'react';
import { authService } from '../../services/authService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
// Tipo de tarifa se maneja por defecto como SIMPLE (no se muestra dropdown)

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

export default function HorasExtraView() {
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
  const [fecha, setFecha] = useState<string>(new Date().toISOString().slice(0, 10));
  const [horas, setHoras] = useState<number>(1);
  const [motivo, setMotivo] = useState<string>('');
  const [tipo, setTipo] = useState<string>('SIMPLE');
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
      setLista(data || []);
    } catch (e) {
      console.error(e);
      toast.error('No se pudo cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      // Obtener idEmpleado del token
      const userInfoRaw = localStorage.getItem('token');
      let idEmpleado: number | undefined = undefined;
      try {
        idEmpleado = (await import('../../services/authService')).authService.getUserInfo().idEmpleado;
      } catch (err) {
        // fallback: intentar leer directamente
        const ui = (await import('../../services/authService')).authService.getUserInfo();
        idEmpleado = ui.idEmpleado;
      }

      if (!idEmpleado) {
        toast.error('No se pudo determinar el empleado autenticado. Inicie sesión nuevamente.');
        return;
      }

      const payload = {
        fechaSolicitud: fecha,
        cantidadDeHoras: horas,
        motivo,
        tipoTarifa: tipo,
        aprobado: false,
        procesado: false,
        estadoSolicitud: 'PENDIENTE',
        idEmpleado: idEmpleado,
      };
      const res = await fetch(`${API_BASE}/api/horas-extra/solicitar`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Error en la solicitud');
      }
      toast.success('Solicitud enviada');
      setMotivo('');
      setHoras(1);
      fetchLista();
    } catch (err: any) {
      toast.error(err.message || 'Error');
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
          <CardTitle>Solicitar Horas Extra</CardTitle>
          <CardDescription>Solicita hasta 3 horas extra para hoy o ayer.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="space-y-1">
              <Label>Fecha</Label>
              <Input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />
            </div>

            <div className="space-y-1">
              <Label>Horas (max 3)</Label>
              <Input type="number" min={1} max={3} value={horas} onChange={(e) => setHoras(Number(e.target.value))} />
            </div>

            {/* Tipo de tarifa: siempre SIMPLE por defecto (campo oculto) */}

            <div className="md:col-span-2 space-y-1">
              <Label>Motivo</Label>
              <Textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} />
            </div>

            <div className="md:col-span-2 flex justify-end">
              <Button type="submit">Enviar solicitud</Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Solicitudes</CardTitle>
          <CardDescription>Lista de solicitudes de horas extra</CardDescription>
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
                    <th className="py-2 px-3 w-24">Horas</th>
                    <th className="py-2 px-3 w-48">Estado</th>
                    {role !== 'EMPLEADO' && (
                      <th className="py-2 px-3 w-48">Acciones</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {lista.map((item) => (
                    <tr key={item.id} className="border-t">
                      <td className="py-2 px-3">{item.nombreEmpleado} {item.primerApellidoEmpleado}</td>
                      <td className="py-2 px-3">{item.fechaSolicitud}</td>
                      <td className="py-2 px-3">{item.cantidadDeHoras}</td>
                      <td className="py-2 px-3">{formatoEstado(item.estadoSolicitud)}</td>
                      {role !== 'EMPLEADO' && (
                        <td className="py-2 px-3">
                          <div className="flex gap-2">
                            {/* JEFE puede actuar sólo cuando está PENDIENTE */}
                            {role === 'JEFE' && item.estadoSolicitud === 'PENDIENTE' && (
                              <>
                                <Button variant="ghost" size="sm" onClick={() => handleAprobar(item.id!)}>Aprobar</Button>
                                <Button variant="destructive" size="sm" onClick={() => handleRechazar(item.id!)}>Rechazar</Button>
                              </>
                            )}

                            {/* RH/ADMIN puede actuar cuando está PENDIENTE_RH o APROBADA_POR_JEFE */}
                            {(role === 'HR' || role === 'ADMIN') && (item.estadoSolicitud === 'PENDIENTE_RH' || item.estadoSolicitud === 'APROBADA_POR_JEFE') && (
                              <>
                                <Button variant="ghost" size="sm" onClick={() => handleAprobar(item.id!)}>Aprobar</Button>
                                <Button variant="destructive" size="sm" onClick={() => handleRechazar(item.id!)}>Rechazar</Button>
                              </>
                            )}
                          </div>
                        </td>
                      )}
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
