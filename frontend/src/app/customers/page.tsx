"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { Eye, Pencil, Trash2 } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuthGuard } from "@/hooks/use-auth-guard";
import { createCustomer, listCustomers, updateCustomer, deleteCustomer, type Customer, type CustomerInput } from "@/lib/api/crm";

const emptyCustomer: CustomerInput = { firstName: "", lastName: "", email: "", phone: "", city: "", age: undefined, gender: "UNSPECIFIED" };

export default function CustomersPage() {
  useAuthGuard();
  const queryClient = useQueryClient();
  const router = useRouter();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [editingCustomerId, setEditingCustomerId] = useState<string | null>(null);
  const [form, setForm] = useState<CustomerInput>(emptyCustomer);

  const customers = useQuery({ 
    queryKey: ["customers", search, page], 
    queryFn: () => listCustomers({ search: search || undefined, page, size: 10 }) 
  });

  const save = useMutation({
    mutationFn: () => editingCustomerId ? updateCustomer(editingCustomerId, form) : createCustomer(form),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["customers"] });
      setForm(emptyCustomer);
      setEditingCustomerId(null);
    }
  });

  const remove = useMutation({
    mutationFn: (id: string) => deleteCustomer(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["customers"] });
    }
  });

  const rows = customers.data?.content ?? [];

  const handleEdit = (e: React.MouseEvent, customer: Customer) => {
    e.stopPropagation();
    setEditingCustomerId(customer.id);
    setForm({
      firstName: customer.firstName,
      lastName: customer.lastName,
      email: customer.email,
      phone: customer.phone,
      city: customer.city,
      age: customer.age,
      gender: customer.gender,
    });
  };

  const handleDelete = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    if (window.confirm("Are you sure you want to delete this customer?")) {
      remove.mutate(id);
    }
  };

  return (
    <AppShell title="Customers" eyebrow="Customer Management">
      <section className="grid gap-6 lg:grid-cols-[300px_1fr]">
        <div className="rounded-lg border bg-card p-4 h-fit">
          <h2 className="font-semibold mb-4">{editingCustomerId ? "Edit Customer" : "New Customer"}</h2>
          <div className="space-y-4">
            <Field label="First name" value={form.firstName} onChange={(v) => setForm({ ...form, firstName: v })} />
            <Field label="Last name" value={form.lastName} onChange={(v) => setForm({ ...form, lastName: v })} />
            <Field label="Email" type="email" value={form.email} onChange={(v) => setForm({ ...form, email: v })} />
            <Field label="City" value={form.city ?? ""} onChange={(v) => setForm({ ...form, city: v })} />
            <Field
  label="Phone"
  value={form.phone ?? ""}
  onChange={(v) => setForm({ ...form, phone: v })}
/>

<Field
  label="Age"
  type="number"
  value={form.age?.toString() ?? ""}
  onChange={(v) =>
    setForm({
      ...form,
      age: parseInt(v) || undefined,
    })
  }
/>

<div className="space-y-2">
  <Label>Gender</Label>

  <select
    value={form.gender}
    onChange={(e) =>
      setForm({
        ...form,
        gender: e.target.value as
          | "MALE"
          | "FEMALE"
          | "UNSPECIFIED",
      })
    }
    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
  >
    <option value="MALE">Male</option>
    <option value="FEMALE">Female</option>
    <option value="UNSPECIFIED">Unspecified</option>
  </select>
</div>
            <div className="flex gap-2">
              <Button className="flex-1" onClick={() => save.mutate()} disabled={save.isPending}>
                {editingCustomerId ? "Update Customer" : "Save"}
              </Button>
              {editingCustomerId && (
                <Button variant="outline" onClick={() => { setEditingCustomerId(null); setForm(emptyCustomer); }}>Cancel</Button>
              )}
            </div>
          </div>
        </div>
        <div className="rounded-lg border bg-card">
          <div className="p-4 border-b">
            <Input placeholder="Search customers..." value={search} onChange={(e) => setSearch(e.target.value)} className="max-w-sm" />
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="border-b bg-muted/50">
                <tr>
                  <th className="p-3 font-medium">Name</th>
                  <th className="p-3 font-medium">Email</th>
                  <th className="p-3 font-medium">Phone</th>
                  <th className="p-3 font-medium">City</th>
                  <th className="p-3 font-medium">Age</th>
                  <th className="p-3 font-medium">Gender</th>
                  <th className="p-3 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((customer) => (
                  <tr key={customer.id} className="border-b last:border-0 hover:bg-muted/50 cursor-pointer" onClick={() => router.push(`/customers/${customer.id}`)}>
                    <td className="p-3">{customer.firstName} {customer.lastName}</td>
                    <td className="p-3">{customer.email}</td>
                    <td className="p-3">{customer.phone ?? "-"}</td>
                    <td className="p-3">{customer.city}</td>
                    <td className="p-3">{customer.age ?? "-"}</td>
                    <td className="p-3">{customer.gender}</td>
                    <td className="p-3 text-right">
                      <div className="flex justify-end gap-2">
                        {/* <Button variant="outline" size="sm" onClick={(e) => { e.stopPropagation(); router.push(`/customers/${customer.id}`); }}>
                          <Eye className="mr-2 h-4 w-4" />Profile
                        </Button> */}
                        <Button variant="outline" size="sm" onClick={(e) => handleEdit(e, customer)}>
                          <Pencil className="mr-2 h-4 w-4" />Edit
                        </Button>
                        <Button variant="destructive" size="sm" onClick={(e) => handleDelete(e, customer.id)} disabled={remove.isPending}>
                          <Trash2 className="mr-2 h-4 w-4" />Delete
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {customers.data && <Pager page={page} totalPages={customers.data.totalPages} setPage={setPage} />}
        </div>
      </section>
    </AppShell>
  );
}

function Field({ label, value, type = "text", onChange }: { label: string; value: string; type?: string; onChange: (v: string) => void }) {
  return (
    <div className="space-y-2">
      <Label>{label}</Label>
      <Input type={type} value={value} onChange={(event) => onChange(event.target.value)} required={["First name", "Last name", "Email"].includes(label)} />
    </div>
  );
}

function Pager({ page, totalPages, setPage }: { page: number; totalPages: number; setPage: (page: number) => void }) {
  return (
    <div className="flex items-center justify-end gap-2 border-t p-3">
      <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</Button>
      <span className="text-sm text-muted-foreground">Page {page + 1} of {Math.max(totalPages, 1)}</span>
      <Button variant="outline" size="sm" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</Button>
    </div>
  );
}